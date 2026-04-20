package com.jaasielsilva.erpcorporativo.app.controller.web.tenantadmin;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.commercial.TenantBillingProfileRequest;
import com.jaasielsilva.erpcorporativo.app.exception.AppException;
import com.jaasielsilva.erpcorporativo.app.service.api.v1.tenantadmin.TenantCommercialApiService;
import com.jaasielsilva.erpcorporativo.app.service.shared.MercadoPagoBillingService;
import com.jaasielsilva.erpcorporativo.app.service.web.tenantadmin.TenantPortalWebService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/app/minha-conta")
@RequiredArgsConstructor
public class TenantCommercialWebController {

    private final TenantPortalWebService tenantPortalWebService;
    private final TenantCommercialApiService tenantCommercialApiService;
    private final MercadoPagoBillingService mercadoPagoBillingService;

    @GetMapping
    public String index(Authentication authentication, Model model) {
        populateCommon(authentication, model, "Minha Conta", "Plano, cobranca e onboarding");
        model.addAttribute("profile", tenantCommercialApiService.profile(authentication));
        return "tenant/commercial/account";
    }

    @GetMapping("/faturas")
    public String invoices(
            Authentication authentication,
            @RequestParam(name = "payment_id", required = false) String paymentId,
            @RequestParam(name = "collection_id", required = false) String collectionId,
            Model model
    ) {
        // Fallback de sincronização quando o usuário volta do checkout e o webhook ainda não chegou.
        String paymentToSync = paymentId != null && !paymentId.isBlank() ? paymentId : collectionId;
        if (paymentToSync != null && !paymentToSync.isBlank()) {
            try {
                mercadoPagoBillingService.processPaymentWebhook(paymentToSync);
            } catch (AppException ignored) {
                // Não interrompe a experiência da tela de faturas em caso de falha transitória.
            }
        }
        populateCommon(authentication, model, "Faturas", "Assinatura e pagamentos");
        model.addAttribute("invoices", tenantCommercialApiService.listInvoices(authentication));
        return "tenant/commercial/invoices";
    }

    @PostMapping("/faturas/{paymentId}/checkout")
    public String checkout(
            Authentication authentication,
            @PathVariable("paymentId") Long paymentId,
            RedirectAttributes redirectAttributes
    ) {
        try {
            var checkout = tenantCommercialApiService.generateCheckout(authentication, paymentId);
            if (checkout.checkoutUrl() == null || checkout.checkoutUrl().isBlank()) {
                redirectAttributes.addFlashAttribute("toastError", "Falha ao gerar checkout.");
                return "redirect:/app/minha-conta/faturas";
            }
            return "redirect:" + checkout.checkoutUrl();
        } catch (AppException ex) {
            redirectAttributes.addFlashAttribute("toastError", ex.getMessage());
            return "redirect:/app/minha-conta/faturas";
        }
    }

    @PostMapping("/billing/mensal")
    public String setMonthly(Authentication authentication, RedirectAttributes redirectAttributes) {
        updateBillingCycle(authentication, redirectAttributes, com.jaasielsilva.erpcorporativo.app.model.BillingCycle.MENSAL);
        return "redirect:/app/minha-conta";
    }

    @PostMapping("/billing/anual")
    public String setAnnual(Authentication authentication, RedirectAttributes redirectAttributes) {
        updateBillingCycle(authentication, redirectAttributes, com.jaasielsilva.erpcorporativo.app.model.BillingCycle.ANUAL);
        return "redirect:/app/minha-conta";
    }

    private void updateBillingCycle(
            Authentication authentication,
            RedirectAttributes redirectAttributes,
            com.jaasielsilva.erpcorporativo.app.model.BillingCycle cycle
    ) {
        try {
            var profile = tenantCommercialApiService.profile(authentication).billingProfile();
            tenantCommercialApiService.updateBillingProfile(
                    authentication,
                    new TenantBillingProfileRequest(
                            profile.billingEmail(),
                            cycle,
                            profile.autoRenew(),
                            profile.selfServiceEnabled()
                    )
            );
            redirectAttributes.addFlashAttribute("toastSuccess", "Ciclo de cobrança atualizado para " + cycle + ".");
        } catch (AppException ex) {
            redirectAttributes.addFlashAttribute("toastError", ex.getMessage());
        }
    }

    private void populateCommon(Authentication authentication, Model model, String title, String subtitle) {
        model.addAttribute("tenantModules", tenantPortalWebService.listEnabledModules(authentication));
        model.addAttribute("activeMenu", "dashboard");
        model.addAttribute("pageTitle", title);
        model.addAttribute("pageSubtitle", subtitle);
    }
}
