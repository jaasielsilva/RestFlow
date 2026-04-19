package com.jaasielsilva.erpcorporativo.app.mapper.web.home;

import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Component;

import com.jaasielsilva.erpcorporativo.app.dto.web.home.HomeRecentLogViewModel;
import com.jaasielsilva.erpcorporativo.app.dto.web.home.HomeRecentTenantViewModel;
import com.jaasielsilva.erpcorporativo.app.dto.web.home.HomeViewModel;
import com.jaasielsilva.erpcorporativo.app.model.AuditLog;
import com.jaasielsilva.erpcorporativo.app.model.Tenant;

@Component
public class HomeWebMapper {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public HomeViewModel toViewModel(
            String email,
            long tenantsAtivos,
            long usuariosAtivos,
            long modulosAtivos,
            long mrrTotal,
            List<Tenant> recentTenants,
            List<String> chartLabels,
            List<Long> chartTenants,
            List<Long> chartUsuarios,
            List<AuditLog> recentLogs
    ) {
        List<HomeRecentTenantViewModel> recentTenantsView = recentTenants.stream()
                .map(t -> new HomeRecentTenantViewModel(t.getNome(), t.isAtivo(), t.getCreatedAt()))
                .toList();

        List<HomeRecentLogViewModel> logsView = recentLogs.stream()
                .map(l -> new HomeRecentLogViewModel(
                        l.getAcao() != null ? l.getAcao().name() : "-",
                        l.getDescricao(),
                        l.getCreatedAt() != null ? l.getCreatedAt().format(FMT) : "-"
                ))
                .toList();

        return new HomeViewModel(
                email,
                tenantsAtivos,
                usuariosAtivos,
                modulosAtivos,
                mrrTotal,
                recentTenantsView,
                chartLabels,
                chartTenants,
                chartUsuarios,
                logsView
        );
    }
}
