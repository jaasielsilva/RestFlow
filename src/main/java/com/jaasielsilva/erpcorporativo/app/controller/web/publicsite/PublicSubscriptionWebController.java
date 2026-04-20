package com.jaasielsilva.erpcorporativo.app.controller.web.publicsite;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.jaasielsilva.erpcorporativo.app.dto.web.publicsite.PublicSubscriptionForm;
import com.jaasielsilva.erpcorporativo.app.exception.AppException;
import com.jaasielsilva.erpcorporativo.app.model.OnboardingSubscription;
import com.jaasielsilva.erpcorporativo.app.service.shared.MercadoPagoBillingService;
import com.jaasielsilva.erpcorporativo.app.service.web.publicsite.PublicSubscriptionWebService;
import com.jaasielsilva.erpcorporativo.app.security.SecurityRequestUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class PublicSubscriptionWebController {

    private final PublicSubscriptionWebService publicSubscriptionWebService;
    private final MercadoPagoBillingService mercadoPagoBillingService;

    @GetMapping("/planos")
    public String plans(Model model) {
        populatePlanViewModel(model);
        return "public/plans";
    }

    @PostMapping("/planos/assinar")
    public String startSubscription(
            @Valid @ModelAttribute("subscriptionForm") PublicSubscriptionForm form,
            BindingResult bindingResult,
            HttpServletRequest request,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            populatePlanViewModel(model);
            return "public/plans";
        }

        try {
            String baseUrl = resolveBaseUrl(request);
            String originIp = SecurityRequestUtils.extractClientKey(request);
            String userAgent = request.getHeader("User-Agent");
            var result = publicSubscriptionWebService.startSubscription(form, baseUrl, originIp, userAgent);
            return "redirect:" + result.checkoutUrl();
        } catch (AppException ex) {
            bindingResult.reject("subscription.error", ex.getMessage());
            populatePlanViewModel(model);
            return "public/plans";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute(
                    "toastError",
                    "Não foi possível iniciar sua assinatura agora. Tente novamente em alguns minutos."
            );
            return "redirect:/planos";
        }
    }

    @GetMapping("/assinatura/status")
    public String subscriptionStatus(
            @RequestParam(name = "state", required = false, defaultValue = "pending") String state,
            @RequestParam(name = "payment_id", required = false) String paymentId,
            @RequestParam(name = "collection_id", required = false) String collectionId,
            @RequestParam(name = "external_reference", required = false) String externalReference,
            Model model
    ) {
        String paymentProviderId = paymentId != null && !paymentId.isBlank() ? paymentId : collectionId;
        if (paymentProviderId != null && !paymentProviderId.isBlank()) {
            try {
                var syncResult = mercadoPagoBillingService.reconcilePaymentByProviderId(paymentProviderId);
                if ((externalReference == null || externalReference.isBlank())
                        && syncResult.externalReference() != null
                        && !syncResult.externalReference().isBlank()) {
                    externalReference = syncResult.externalReference();
                }
            } catch (RuntimeException ignored) {
                // Mantém a página de status disponível mesmo com falha pontual na reconciliação.
            }
        }

        OnboardingSubscription onboarding = publicSubscriptionWebService.findByExternalReference(externalReference);
        model.addAttribute("state", state);
        model.addAttribute("onboarding", onboarding);
        return "public/subscription-status";
    }

    private void populatePlanViewModel(Model model) {
        var plans = publicSubscriptionWebService.listActivePlans();
        model.addAttribute("plans", plans);
        model.addAttribute("entryPrice", publicSubscriptionWebService.resolveEntryPrice());
        if (!model.containsAttribute("subscriptionForm")) {
            PublicSubscriptionForm form = new PublicSubscriptionForm();
            if (!plans.isEmpty()) {
                form.setPlanId(plans.get(0).getId());
            }
            model.addAttribute("subscriptionForm", form);
        }
    }

    private String resolveBaseUrl(HttpServletRequest request) {
        String forwardedProto = request.getHeader("X-Forwarded-Proto");
        String forwardedHost = request.getHeader("X-Forwarded-Host");
        String scheme = (forwardedProto != null && !forwardedProto.isBlank()) ? forwardedProto : request.getScheme();
        String host = (forwardedHost != null && !forwardedHost.isBlank()) ? forwardedHost : request.getServerName();
        int port = request.getServerPort();

        boolean defaultPort = ("http".equalsIgnoreCase(scheme) && port == 80)
                || ("https".equalsIgnoreCase(scheme) && port == 443);
        if (host.contains(":")) {
            return scheme + "://" + host;
        }
        if (defaultPort) {
            return scheme + "://" + host;
        }
        return scheme + "://" + host + ":" + port;
    }
}
