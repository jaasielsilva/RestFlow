package com.jaasielsilva.erpcorporativo.app.usecase.web.admin;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.jaasielsilva.erpcorporativo.app.dto.web.admin.AdminDashboardViewModel;
import com.jaasielsilva.erpcorporativo.app.dto.web.admin.PlatformChartPointViewModel;
import com.jaasielsilva.erpcorporativo.app.dto.web.admin.PlatformMetricViewModel;
import com.jaasielsilva.erpcorporativo.app.dto.web.admin.RestaurantRowViewModel;
import com.jaasielsilva.erpcorporativo.app.dto.web.admin.contract.ContractAlertViewModel;
import com.jaasielsilva.erpcorporativo.app.dto.web.admin.contract.ContractKpiViewModel;
import com.jaasielsilva.erpcorporativo.app.mapper.web.admin.AdminDashboardWebMapper;
import com.jaasielsilva.erpcorporativo.app.model.Contract;
import com.jaasielsilva.erpcorporativo.app.model.ContractStatus;
import com.jaasielsilva.erpcorporativo.app.model.PaymentStatus;
import com.jaasielsilva.erpcorporativo.app.model.Role;
import com.jaasielsilva.erpcorporativo.app.model.Tenant;
import com.jaasielsilva.erpcorporativo.app.repository.contract.ContractRepository;
import com.jaasielsilva.erpcorporativo.app.repository.contract.PaymentRecordRepository;
import com.jaasielsilva.erpcorporativo.app.repository.tenant.TenantRepository;
import com.jaasielsilva.erpcorporativo.app.repository.usuario.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BuildAdminDashboardUseCase {

    private final UsuarioRepository usuarioRepository;
    private final TenantRepository tenantRepository;
    private final AdminDashboardWebMapper adminDashboardWebMapper;
    private final ContractRepository contractRepository;
    private final PaymentRecordRepository paymentRecordRepository;

    public AdminDashboardViewModel execute(Authentication authentication) {
        List<Tenant> restaurants = tenantRepository.findAll().stream()
                .filter(this::isRestaurantTenant)
                .sorted(Comparator.comparing(Tenant::getId).reversed())
                .toList();

        long totalUsuarios = restaurants.stream()
                .mapToLong(tenant -> usuarioRepository.countByTenantId(tenant.getId()))
                .sum();
        long totalTenants = restaurants.size();
        long restaurantesAtivos = restaurants.stream().filter(Tenant::isAtivo).count();
        long restaurantesTrial = restaurants.stream()
                .filter(tenant -> resolveRestaurantStatus(tenant).equals("Trial"))
                .count();
        long novosCadastrosMes = restaurants.stream()
                .filter(this::isCurrentMonth)
                .count();
        long mrr = restaurants.stream()
                .mapToLong(this::resolveMonthlyRevenue)
                .sum();

        ContractKpiViewModel contractKpis = buildContractKpis();
        List<ContractAlertViewModel> contractAlerts = buildContractAlerts();

        return adminDashboardWebMapper.toViewModel(
                authentication.getName(),
                totalUsuarios,
                totalTenants,
                buildMetrics(restaurantesAtivos, restaurantesTrial, mrr, novosCadastrosMes),
                buildGrowthChart(restaurants),
                buildRevenueChart(restaurants),
                buildPlanDistribution(restaurants),
                buildRestaurantRows(restaurants),
                contractKpis,
                contractAlerts
        );
    }

    private ContractKpiViewModel buildContractKpis() {
        long totalAtivos = contractRepository.countByStatus(ContractStatus.ATIVO);
        long totalVencidos = contractRepository.countByStatusAndDataTerminoBefore(ContractStatus.ATIVO, LocalDate.now());
        long totalAtrasados = paymentRecordRepository.countByStatus(PaymentStatus.ATRASADO);
        BigDecimal mrrTotal = contractRepository.sumValorMensalByStatus(ContractStatus.ATIVO);
        return new ContractKpiViewModel(totalAtivos, totalVencidos, totalAtrasados, mrrTotal);
    }

    private List<ContractAlertViewModel> buildContractAlerts() {
        LocalDate hoje = LocalDate.now();
        LocalDate em30Dias = hoje.plusDays(30);

        List<Contract> vencendo = contractRepository.findByStatusAndDataTerminoBetween(
                ContractStatus.ATIVO, hoje, em30Dias);

        List<ContractAlertViewModel> alerts = new ArrayList<>();
        for (Contract c : vencendo) {
            boolean pagamentoAtrasado = paymentRecordRepository
                    .findFirstByContractIdOrderByMesReferenciaDesc(c.getId())
                    .map(p -> p.getStatus() == PaymentStatus.ATRASADO)
                    .orElse(false);
            alerts.add(new ContractAlertViewModel(
                    c.getId(),
                    c.getTenant().getNome(),
                    c.getDataTermino(),
                    true,
                    pagamentoAtrasado
            ));
        }
        return alerts;
    }

    private boolean isRestaurantTenant(Tenant tenant) {
        return tenant.getSlug() != null && !"platform".equalsIgnoreCase(tenant.getSlug());
    }

    private boolean isCurrentMonth(Tenant tenant) {
        if (tenant.getCreatedAt() == null) {
            return false;
        }

        LocalDate now = LocalDate.now();
        return tenant.getCreatedAt().getYear() == now.getYear()
                && tenant.getCreatedAt().getMonth() == now.getMonth();
    }

    private List<PlatformMetricViewModel> buildMetrics(
            long restaurantesAtivos,
            long restaurantesTrial,
            long mrr,
            long novosCadastrosMes
    ) {
        return List.of(
                new PlatformMetricViewModel(
                        "Total de Restaurantes Ativos",
                        String.valueOf(restaurantesAtivos),
                        "+12.4%",
                        true,
                        "store"
                ),
                new PlatformMetricViewModel(
                        "Restaurantes em Teste Grátis",
                        String.valueOf(restaurantesTrial),
                        "+4.1%",
                        true,
                        "sparkles"
                ),
                new PlatformMetricViewModel(
                        "Receita Mensal (MRR)",
                        "R$ " + mrr,
                        "+8.7%",
                        true,
                        "wallet"
                ),
                new PlatformMetricViewModel(
                        "Novos Cadastros no Mês",
                        String.valueOf(novosCadastrosMes),
                        "+15.2%",
                        true,
                        "chart"
                )
        );
    }

    private List<PlatformChartPointViewModel> buildGrowthChart(List<Tenant> restaurants) {
        int total = Math.max(restaurants.size(), 1);
        return buildLastSixMonths().stream()
                .map(label -> new PlatformChartPointViewModel(label, estimateValue(total, label, 2)))
                .toList();
    }

    private List<PlatformChartPointViewModel> buildRevenueChart(List<Tenant> restaurants) {
        int base = Math.max(restaurants.stream().mapToInt(tenant -> (int) resolveMonthlyRevenue(tenant)).sum(), 1200);
        return buildLastSixMonths().stream()
                .map(label -> new PlatformChartPointViewModel(label, estimateValue(base, label, 7)))
                .toList();
    }

    private List<PlatformChartPointViewModel> buildPlanDistribution(List<Tenant> restaurants) {
        long start = restaurants.stream().filter(tenant -> resolvePlanName(tenant).equals("Start")).count();
        long growth = restaurants.stream().filter(tenant -> resolvePlanName(tenant).equals("Growth")).count();
        long scale = restaurants.stream().filter(tenant -> resolvePlanName(tenant).equals("Scale")).count();

        return List.of(
                new PlatformChartPointViewModel("Start", (int) start),
                new PlatformChartPointViewModel("Growth", (int) growth),
                new PlatformChartPointViewModel("Scale", (int) scale)
        );
    }

    private List<RestaurantRowViewModel> buildRestaurantRows(List<Tenant> restaurants) {
        return restaurants.stream()
                .limit(8)
                .map(tenant -> new RestaurantRowViewModel(
                        tenant.getId(),
                        tenant.getNome(),
                        usuarioRepository.findFirstByTenantIdAndRoleOrderByIdAsc(tenant.getId(), Role.ADMIN)
                                .map(admin -> admin.getEmail())
                                .orElse("admin@" + tenant.getSlug() + ".com"),
                        resolvePlanName(tenant),
                        resolveRestaurantStatus(tenant),
                        tenant.getCreatedAt()
                ))
                .toList();
    }

    private String resolvePlanName(Tenant tenant) {
        long totalUsuarios = usuarioRepository.countByTenantId(tenant.getId());

        if (totalUsuarios <= 5) {
            return "Start";
        }

        if (totalUsuarios <= 15) {
            return "Growth";
        }

        return "Scale";
    }

    private String resolveRestaurantStatus(Tenant tenant) {
        if (!tenant.isAtivo()) {
            return "Suspenso";
        }

        LocalDateTime createdAt = tenant.getCreatedAt();
        if (createdAt != null && createdAt.isAfter(LocalDateTime.now().minusDays(14))) {
            return "Trial";
        }

        return "Ativo";
    }

    private long resolveMonthlyRevenue(Tenant tenant) {
        return switch (resolvePlanName(tenant)) {
            case "Start" -> 149L;
            case "Growth" -> 299L;
            default -> 549L;
        };
    }

    private List<String> buildLastSixMonths() {
        LocalDate now = LocalDate.now();
        return java.util.stream.IntStream.rangeClosed(5, 0)
                .mapToObj(now::minusMonths)
                .map(date -> date.getMonth().getDisplayName(TextStyle.SHORT, new Locale("pt", "BR")))
                .toList();
    }

    private int estimateValue(int base, String label, int divisor) {
        int hash = Math.abs(label.hashCode());
        return Math.max(1, (base / divisor) + (hash % Math.max(base / (divisor + 1), 6)));
    }
}
