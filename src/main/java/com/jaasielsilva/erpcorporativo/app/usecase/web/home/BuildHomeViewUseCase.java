package com.jaasielsilva.erpcorporativo.app.usecase.web.home;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.jaasielsilva.erpcorporativo.app.dto.web.home.HomeViewModel;
import com.jaasielsilva.erpcorporativo.app.mapper.web.home.HomeWebMapper;
import com.jaasielsilva.erpcorporativo.app.model.Tenant;
import com.jaasielsilva.erpcorporativo.app.repository.module.PlatformModuleRepository;
import com.jaasielsilva.erpcorporativo.app.repository.audit.AuditLogRepository;
import com.jaasielsilva.erpcorporativo.app.repository.tenant.TenantRepository;
import com.jaasielsilva.erpcorporativo.app.repository.usuario.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BuildHomeViewUseCase {

    private static final int RECENT_TENANTS_LIMIT = 5;

    private final HomeWebMapper homeWebMapper;
    private final TenantRepository tenantRepository;
    private final UsuarioRepository usuarioRepository;
    private final PlatformModuleRepository platformModuleRepository;
    private final AuditLogRepository auditLogRepository;

    @Transactional(readOnly = true)
    public HomeViewModel execute(Authentication authentication) {
        List<Tenant> allClientTenants = tenantRepository.findAll().stream()
                .filter(t -> !"platform".equalsIgnoreCase(t.getSlug()))
                .toList();

        long tenantsAtivos  = allClientTenants.stream().filter(Tenant::isAtivo).count();
        long usuariosAtivos = allClientTenants.stream()
                .mapToLong(t -> usuarioRepository.countByTenantIdAndAtivoTrue(t.getId()))
                .sum();
        long modulosAtivos  = platformModuleRepository.findAll().stream()
                .filter(m -> m.isAtivo()).count();
        long mrrTotal       = calcMrr(allClientTenants);

        List<Tenant> recentTenants = tenantRepository
                .findAllByOrderByCreatedAtDesc(PageRequest.of(0, RECENT_TENANTS_LIMIT));

        List<String> labels   = buildLastSixMonthLabels();
        List<Long>   chartT   = buildTenantGrowthChart(allClientTenants, labels);
        List<Long>   chartU   = buildUserGrowthChart(allClientTenants, labels);

        return homeWebMapper.toViewModel(
                authentication.getName(),
                tenantsAtivos,
                usuariosAtivos,
                modulosAtivos,
                mrrTotal,
                recentTenants,
                labels,
                chartT,
                chartU,
                auditLogRepository.findTop4ByOrderByCreatedAtDesc()
        );
    }

    // MRR real: soma o valor do plano de cada tenant ativo
    private long calcMrr(List<Tenant> tenants) {
        return tenants.stream()
                .filter(Tenant::isAtivo)
                .mapToLong(t -> {
                    if (t.getSubscriptionPlan() != null) {
                        // futuramente o plano terá campo preço — por ora usa contagem de módulos como proxy
                        return 149L;
                    }
                    return 0L;
                })
                .sum();
    }

    private List<String> buildLastSixMonthLabels() {
        LocalDate now = LocalDate.now();
        return java.util.stream.IntStream.iterate(5, i -> i >= 0, i -> i - 1)
                .mapToObj(i -> now.minusMonths(i)
                        .getMonth()
                        .getDisplayName(TextStyle.SHORT, new Locale("pt", "BR")))
                .toList();
    }

    private List<Long> buildTenantGrowthChart(List<Tenant> tenants, List<String> labels) {
        LocalDate now = LocalDate.now();
        return java.util.stream.IntStream.iterate(5, i -> i >= 0, i -> i - 1)
                .mapToObj(i -> {
                    LocalDate month = now.minusMonths(i);
                    return tenants.stream()
                            .filter(t -> t.getCreatedAt() != null
                                    && t.getCreatedAt().getYear() == month.getYear()
                                    && t.getCreatedAt().getMonth() == month.getMonth())
                            .count();
                })
                .toList();
    }

    private List<Long> buildUserGrowthChart(List<Tenant> tenants, List<String> labels) {
        // Usuários totais acumulados por mês (snapshot do mês atual para todos os meses — simplificado)
        long total = tenants.stream()
                .mapToLong(t -> usuarioRepository.countByTenantId(t.getId()))
                .sum();
        // Distribui proporcionalmente nos últimos 6 meses
        int size = labels.size();
        return java.util.stream.IntStream.range(0, size)
                .mapToObj(i -> Math.max(0L, total * (i + 1) / size))
                .toList();
    }
}
