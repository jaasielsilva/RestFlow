package com.jaasielsilva.erpcorporativo.app.controller.web.admin;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.transaction.annotation.Transactional;

import com.jaasielsilva.erpcorporativo.app.model.AuditAction;
import com.jaasielsilva.erpcorporativo.app.model.AuditLog;
import com.jaasielsilva.erpcorporativo.app.model.Tenant;
import com.jaasielsilva.erpcorporativo.app.repository.audit.AuditLogRepository;
import com.jaasielsilva.erpcorporativo.app.repository.module.PlatformModuleRepository;
import com.jaasielsilva.erpcorporativo.app.repository.plan.SubscriptionPlanRepository;
import com.jaasielsilva.erpcorporativo.app.repository.tenant.TenantRepository;
import com.jaasielsilva.erpcorporativo.app.repository.usuario.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/reports")
@RequiredArgsConstructor
public class AdminReportsWebController {

    private final TenantRepository tenantRepository;
    private final UsuarioRepository usuarioRepository;
    private final PlatformModuleRepository platformModuleRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final AuditLogRepository auditLogRepository;

    @GetMapping
    @Transactional(readOnly = true)
    public String index(Model model) {
        List<Tenant> clientTenants = tenantRepository.findAll().stream()
                .filter(t -> !"platform".equalsIgnoreCase(t.getSlug()))
                .toList();

        long tenantsAtivos   = clientTenants.stream().filter(Tenant::isAtivo).count();
        long tenantsInativos = clientTenants.stream().filter(t -> !t.isAtivo()).count();
        long totalUsuarios   = clientTenants.stream()
                .mapToLong(t -> usuarioRepository.countByTenantId(t.getId())).sum();
        long modulosAtivos   = platformModuleRepository.findAll().stream().filter(m -> m.isAtivo()).count();
        long planosAtivos    = subscriptionPlanRepository.findAll().stream().filter(p -> p.isAtivo()).count();
        long logsHoje        = auditLogRepository.countByCreatedAtAfter(LocalDate.now().atStartOfDay());

        // Tenants por plano
        Map<String, Long> tenantsPorPlano = clientTenants.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getSubscriptionPlan() != null ? t.getSubscriptionPlan().getNome() : "Sem plano",
                        Collectors.counting()
                ));

        // MRR real (149 por tenant ativo com plano — futuramente campo preço no plano)
        long mrr = clientTenants.stream()
                .filter(t -> t.isAtivo() && t.getSubscriptionPlan() != null)
                .count() * 149L;

        // Novos tenants no mês
        LocalDateTime inicioMes = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        long novosMes = clientTenants.stream()
                .filter(t -> t.getCreatedAt() != null && t.getCreatedAt().isAfter(inicioMes))
                .count();

        model.addAttribute("tenantsAtivos", tenantsAtivos);
        model.addAttribute("tenantsInativos", tenantsInativos);
        model.addAttribute("totalUsuarios", totalUsuarios);
        model.addAttribute("modulosAtivos", modulosAtivos);
        model.addAttribute("planosAtivos", planosAtivos);
        model.addAttribute("logsHoje", logsHoje);
        model.addAttribute("mrr", mrr);
        model.addAttribute("novosMes", novosMes);
        model.addAttribute("tenantsPorPlano", tenantsPorPlano);
        model.addAttribute("activeMenu", "reports");
        model.addAttribute("pageTitle", "Relatórios");
        model.addAttribute("pageSubtitle", "Consolidação e análise dos dados da plataforma");
        return "admin/reports/index";
    }

    @GetMapping("/audit")
    public String audit(
            @RequestParam(required = false) String acao,
            @RequestParam(required = false) String entidade,
            @RequestParam(required = false) String executadoPor,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size,
            Model model
    ) {
        AuditAction auditAction = null;
        try {
            if (acao != null && !acao.isBlank()) auditAction = AuditAction.valueOf(acao);
        } catch (IllegalArgumentException ignored) {}

        Page<AuditLog> logs = auditLogRepository.findWithFilters(
                auditAction,
                (entidade != null && !entidade.isBlank()) ? entidade : null,
                (executadoPor != null && !executadoPor.isBlank()) ? executadoPor : null,
                null, null,
                PageRequest.of(page, size)
        );

        model.addAttribute("logs", logs.getContent());
        model.addAttribute("currentPage", logs.getNumber());
        model.addAttribute("totalPages", logs.getTotalPages());
        model.addAttribute("acoes", AuditAction.values());
        model.addAttribute("activeMenu", "reports");
        model.addAttribute("pageTitle", "Auditoria");
        model.addAttribute("pageSubtitle", "Log de ações da plataforma");
        return "admin/reports/audit";
    }
}
