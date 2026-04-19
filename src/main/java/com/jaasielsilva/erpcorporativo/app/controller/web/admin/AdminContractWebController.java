package com.jaasielsilva.erpcorporativo.app.controller.web.admin;

import jakarta.validation.Valid;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.jaasielsilva.erpcorporativo.app.dto.web.admin.contract.ContractForm;
import com.jaasielsilva.erpcorporativo.app.dto.web.admin.contract.ContractViewModel;
import com.jaasielsilva.erpcorporativo.app.dto.web.admin.contract.PaymentRecordForm;
import com.jaasielsilva.erpcorporativo.app.exception.AppException;
import com.jaasielsilva.erpcorporativo.app.model.ContractStatus;
import com.jaasielsilva.erpcorporativo.app.repository.plan.SubscriptionPlanRepository;
import com.jaasielsilva.erpcorporativo.app.repository.tenant.TenantRepository;
import com.jaasielsilva.erpcorporativo.app.usecase.web.admin.ContractUseCase;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/contratos")
@RequiredArgsConstructor
public class AdminContractWebController {

    private final ContractUseCase contractUseCase;
    private final TenantRepository tenantRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;

    @GetMapping
    public String index(
            @RequestParam(required = false) String tenantNome,
            @RequestParam(required = false) ContractStatus status,
            @RequestParam(required = false) String planoCodigo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model
    ) {
        model.addAttribute("view", contractUseCase.list(tenantNome, status, planoCodigo, page, size));
        model.addAttribute("statusValues", ContractStatus.values());
        model.addAttribute("planos", subscriptionPlanRepository.findAll());
        populateCommon(model, "contratos", "Contratos", "Gestão de contratos e assinaturas");
        return "admin/contratos/index";
    }

    @GetMapping("/new")
    public String newContract(
            @RequestParam(required = false) Long tenantId,
            Model model
    ) {
        ContractForm form = new ContractForm();
        if (tenantId != null) form.setTenantId(tenantId);
        model.addAttribute("form", form);
        model.addAttribute("isEdit", false);
        model.addAttribute("statusValues", ContractStatus.values());
        model.addAttribute("tenants", tenantRepository.findAll());
        model.addAttribute("planos", subscriptionPlanRepository.findAll());
        populateCommon(model, "contratos", "Novo Contrato", "Criar contrato para tenant");
        return "admin/contratos/form";
    }

    @PostMapping
    public String create(
            Authentication authentication,
            @Valid @ModelAttribute("form") ContractForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", false);
            model.addAttribute("statusValues", ContractStatus.values());
            model.addAttribute("tenants", tenantRepository.findAll());
            model.addAttribute("planos", subscriptionPlanRepository.findAll());
            populateCommon(model, "contratos", "Novo Contrato", "Criar contrato para tenant");
            return "admin/contratos/form";
        }

        try {
            contractUseCase.create(form, authentication.getName());
            redirectAttributes.addFlashAttribute("toastSuccess", "Contrato criado com sucesso.");
            return "redirect:/admin/contratos";
        } catch (AppException ex) {
            bindingResult.reject("contract.create", ex.getMessage());
            model.addAttribute("isEdit", false);
            model.addAttribute("statusValues", ContractStatus.values());
            model.addAttribute("tenants", tenantRepository.findAll());
            model.addAttribute("planos", subscriptionPlanRepository.findAll());
            populateCommon(model, "contratos", "Novo Contrato", "Criar contrato para tenant");
            return "admin/contratos/form";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        ContractViewModel contract = contractUseCase.getById(id);
        model.addAttribute("contract", contract);
        model.addAttribute("statusValues", ContractStatus.values());
        model.addAttribute("paymentForm", new PaymentRecordForm());
        populateCommon(model, "contratos", "Contrato " + contract.tenantNome(), contract.status().name());
        return "admin/contratos/detail";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        ContractViewModel contract = contractUseCase.getById(id);

        ContractForm form = new ContractForm();
        form.setTenantId(contract.tenantId());
        form.setSubscriptionPlanId(contract.subscriptionPlanId());
        form.setValorMensal(contract.valorMensal());
        form.setDataInicio(contract.dataInicio());
        form.setDataTermino(contract.dataTermino());
        form.setStatus(contract.status());
        form.setObservacoes(contract.observacoes());
        form.setDiaVencimento(contract.diaVencimento());

        model.addAttribute("form", form);
        model.addAttribute("contractId", id);
        model.addAttribute("isEdit", true);
        model.addAttribute("statusValues", ContractStatus.values());
        model.addAttribute("tenants", tenantRepository.findAll());
        model.addAttribute("planos", subscriptionPlanRepository.findAll());
        populateCommon(model, "contratos", "Editar Contrato", contract.tenantNome());
        return "admin/contratos/form";
    }

    @PostMapping("/{id}")
    public String update(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @ModelAttribute("form") ContractForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("contractId", id);
            model.addAttribute("isEdit", true);
            model.addAttribute("statusValues", ContractStatus.values());
            model.addAttribute("tenants", tenantRepository.findAll());
            model.addAttribute("planos", subscriptionPlanRepository.findAll());
            populateCommon(model, "contratos", "Editar Contrato", "Atualizar contrato");
            return "admin/contratos/form";
        }

        try {
            contractUseCase.update(id, form, authentication.getName());
            redirectAttributes.addFlashAttribute("toastSuccess", "Contrato atualizado com sucesso.");
            return "redirect:/admin/contratos/" + id;
        } catch (AppException ex) {
            bindingResult.reject("contract.update", ex.getMessage());
            model.addAttribute("contractId", id);
            model.addAttribute("isEdit", true);
            model.addAttribute("statusValues", ContractStatus.values());
            model.addAttribute("tenants", tenantRepository.findAll());
            model.addAttribute("planos", subscriptionPlanRepository.findAll());
            populateCommon(model, "contratos", "Editar Contrato", "Atualizar contrato");
            return "admin/contratos/form";
        }
    }

    @PostMapping("/{id}/status")
    public String updateStatus(
            Authentication authentication,
            @PathVariable Long id,
            @RequestParam ContractStatus status,
            RedirectAttributes redirectAttributes
    ) {
        try {
            contractUseCase.updateStatus(id, status, authentication.getName());
            redirectAttributes.addFlashAttribute("toastSuccess", "Status atualizado para " + status + ".");
        } catch (AppException ex) {
            redirectAttributes.addFlashAttribute("toastError", ex.getMessage());
        }
        return "redirect:/admin/contratos/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(
            Authentication authentication,
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {
        try {
            contractUseCase.delete(id, authentication.getName());
            redirectAttributes.addFlashAttribute("toastSuccess", "Contrato removido.");
        } catch (AppException ex) {
            redirectAttributes.addFlashAttribute("toastError", ex.getMessage());
        }
        return "redirect:/admin/contratos";
    }

    @PostMapping("/{id}/payments")
    public String addPayment(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @ModelAttribute("paymentForm") PaymentRecordForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("toastError", "Dados do pagamento inválidos.");
            return "redirect:/admin/contratos/" + id;
        }
        try {
            contractUseCase.addPayment(id, form, authentication.getName());
            redirectAttributes.addFlashAttribute("toastSuccess", "Pagamento registrado.");
        } catch (AppException ex) {
            redirectAttributes.addFlashAttribute("toastError", ex.getMessage());
        }
        return "redirect:/admin/contratos/" + id;
    }

    @PostMapping("/{id}/payments/{pid}")
    public String updatePayment(
            Authentication authentication,
            @PathVariable Long id,
            @PathVariable Long pid,
            @Valid @ModelAttribute("paymentForm") PaymentRecordForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("toastError", "Dados do pagamento inválidos.");
            return "redirect:/admin/contratos/" + id;
        }
        try {
            contractUseCase.updatePayment(id, pid, form, authentication.getName());
            redirectAttributes.addFlashAttribute("toastSuccess", "Pagamento atualizado.");
        } catch (AppException ex) {
            redirectAttributes.addFlashAttribute("toastError", ex.getMessage());
        }
        return "redirect:/admin/contratos/" + id;
    }

    // -------------------------------------------------------------------------

    private void populateCommon(Model model, String activeMenu, String pageTitle, String pageSubtitle) {
        model.addAttribute("activeMenu", activeMenu);
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("pageSubtitle", pageSubtitle);
    }
}
