package com.jaasielsilva.erpcorporativo.app.service.web.tenantadmin;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jaasielsilva.erpcorporativo.app.dto.web.tenantadmin.TenantDashboardViewModel;
import com.jaasielsilva.erpcorporativo.app.dto.web.tenantadmin.TenantDashboardViewModel.OsRecenteViewModel;
import com.jaasielsilva.erpcorporativo.app.dto.web.tenantadmin.TenantDashboardViewModel.ResponsavelCargaViewModel;
import com.jaasielsilva.erpcorporativo.app.dto.web.tenantadmin.TenantPortalModuleViewModel;
import com.jaasielsilva.erpcorporativo.app.model.OrdemServico;
import com.jaasielsilva.erpcorporativo.app.model.OrdemServicoStatus;
import com.jaasielsilva.erpcorporativo.app.model.Role;
import com.jaasielsilva.erpcorporativo.app.repository.os.OrdemServicoRepository;
import com.jaasielsilva.erpcorporativo.app.repository.os.OrdemServicoSpecifications;
import com.jaasielsilva.erpcorporativo.app.repository.usuario.UsuarioRepository;
import com.jaasielsilva.erpcorporativo.app.security.AppUserDetails;
import com.jaasielsilva.erpcorporativo.app.security.SecurityPrincipalUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TenantDashboardWebService {

    private final UsuarioRepository usuarioRepository;
    private final TenantPortalWebService tenantPortalWebService;
    private final OrdemServicoRepository osRepository;

    @Transactional(readOnly = true)
    public TenantDashboardViewModel build(Authentication authentication) {
        AppUserDetails currentUser = SecurityPrincipalUtils.getCurrentUser(authentication);
        Long tenantId = currentUser.getTenantId();
        boolean isAdmin = currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.SUPER_ADMIN;

        // Equipe
        long totalUsuarios = usuarioRepository.countByTenantId(tenantId);
        long usuariosAtivos = usuarioRepository.countByTenantIdAndAtivoTrue(tenantId);
        long adminsAtivos = usuarioRepository.countByTenantIdAndRoleAndAtivoTrue(tenantId, Role.ADMIN);
        List<TenantPortalModuleViewModel> modules = tenantPortalWebService.listEnabledModules(authentication);
        int modulosHabilitados = Math.max(0, modules.size() - 1);

        // OS — todos veem
        long osAbertas = osRepository.countByTenantIdAndStatus(tenantId, OrdemServicoStatus.ABERTA);
        long osEmAndamento = osRepository.countByTenantIdAndStatus(tenantId, OrdemServicoStatus.EM_ANDAMENTO);
        long osConcluidas = osRepository.countByTenantIdAndStatus(tenantId, OrdemServicoStatus.CONCLUIDA);
        long osCanceladas = osRepository.countByTenantIdAndStatus(tenantId, OrdemServicoStatus.CANCELADA);

        List<OsRecenteViewModel> osRecentes = osRepository.findAll(
                OrdemServicoSpecifications.byTenant(tenantId),
                PageRequest.of(0, 5, Sort.by("createdAt").descending())
        ).getContent().stream().map(this::toOsRecente).toList();

        // Financeiro e carga — ADMIN only
        BigDecimal faturamentoMes = BigDecimal.ZERO;
        BigDecimal faturamentoTotal = BigDecimal.ZERO;
        long osPendentesValor = 0;
        List<ResponsavelCargaViewModel> cargaPorResponsavel = List.of();
        List<String> chartLabels = List.of();
        List<Long> chartAbertas = List.of();
        List<Long> chartConcluidas = List.of();

        if (isAdmin) {
            faturamentoMes = calcFaturamentoMes(tenantId);
            faturamentoTotal = calcFaturamentoTotal(tenantId);
            osPendentesValor = osAbertas + osEmAndamento;
            cargaPorResponsavel = buildCargaPorResponsavel(tenantId);
            chartLabels = buildLastSixMonthLabels();
            chartAbertas = buildChartData(tenantId, OrdemServicoStatus.ABERTA, chartLabels);
            chartConcluidas = buildChartData(tenantId, OrdemServicoStatus.CONCLUIDA, chartLabels);
        }

        return new TenantDashboardViewModel(
                totalUsuarios, usuariosAtivos, adminsAtivos, modulosHabilitados,
                osAbertas, osEmAndamento, osConcluidas, osCanceladas, osRecentes,
                faturamentoMes, faturamentoTotal, osPendentesValor,
                cargaPorResponsavel, chartLabels, chartAbertas, chartConcluidas
        );
    }

    private BigDecimal calcFaturamentoMes(Long tenantId) {
        LocalDate inicio = LocalDate.now().withDayOfMonth(1);
        return osRepository.findAll(
                Specification.allOf(
                        OrdemServicoSpecifications.byTenant(tenantId),
                        OrdemServicoSpecifications.byStatus(OrdemServicoStatus.CONCLUIDA)
                )
        ).stream()
                .filter(os -> os.getCreatedAt() != null && os.getCreatedAt().toLocalDate().isAfter(inicio.minusDays(1)))
                .map(os -> os.getValor() != null ? os.getValor() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calcFaturamentoTotal(Long tenantId) {
        return osRepository.findAll(
                Specification.allOf(
                        OrdemServicoSpecifications.byTenant(tenantId),
                        OrdemServicoSpecifications.byStatus(OrdemServicoStatus.CONCLUIDA)
                )
        ).stream()
                .map(os -> os.getValor() != null ? os.getValor() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<ResponsavelCargaViewModel> buildCargaPorResponsavel(Long tenantId) {
        List<OrdemServico> todas = osRepository.findAll(OrdemServicoSpecifications.byTenant(tenantId));
        return todas.stream()
                .filter(os -> os.getResponsavel() != null)
                .collect(java.util.stream.Collectors.groupingBy(os -> os.getResponsavel().getNome()))
                .entrySet().stream()
                .map(e -> new ResponsavelCargaViewModel(
                        e.getKey(),
                        e.getValue().size(),
                        e.getValue().stream().filter(os -> os.getStatus() == OrdemServicoStatus.ABERTA
                                || os.getStatus() == OrdemServicoStatus.EM_ANDAMENTO).count(),
                        e.getValue().stream().filter(os -> os.getStatus() == OrdemServicoStatus.CONCLUIDA).count()
                ))
                .sorted((a, b) -> Long.compare(b.total(), a.total()))
                .limit(5)
                .toList();
    }

    private List<String> buildLastSixMonthLabels() {
        LocalDate now = LocalDate.now();
        List<String> labels = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            labels.add(now.minusMonths(i).getMonth().getDisplayName(TextStyle.SHORT, new Locale("pt", "BR")));
        }
        return labels;
    }

    private List<Long> buildChartData(Long tenantId, OrdemServicoStatus status, List<String> labels) {
        LocalDate now = LocalDate.now();
        List<OrdemServico> all = osRepository.findAll(
                Specification.allOf(
                        OrdemServicoSpecifications.byTenant(tenantId),
                        OrdemServicoSpecifications.byStatus(status)
                )
        );
        List<Long> data = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            LocalDate month = now.minusMonths(i);
            long count = all.stream()
                    .filter(os -> os.getCreatedAt() != null
                            && os.getCreatedAt().getYear() == month.getYear()
                            && os.getCreatedAt().getMonth() == month.getMonth())
                    .count();
            data.add(count);
        }
        return data;
    }

    private OsRecenteViewModel toOsRecente(OrdemServico os) {
        String css = switch (os.getStatus()) {
            case ABERTA -> "bg-primary";
            case EM_ANDAMENTO -> "bg-warning text-dark";
            case CONCLUIDA -> "bg-success";
            case CANCELADA -> "bg-danger";
            default -> "bg-secondary";
        };
        return new OsRecenteViewModel(
                os.getId(), os.getNumero(), os.getTitulo(),
                os.getClienteNome(), os.getStatus().name(), css
        );
    }
}
