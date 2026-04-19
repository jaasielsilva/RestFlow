package com.jaasielsilva.erpcorporativo.app.service.web.admin;

import java.util.List;

import org.springframework.stereotype.Service;

import com.jaasielsilva.erpcorporativo.app.dto.api.PageResponse;
import com.jaasielsilva.erpcorporativo.app.dto.api.admin.tenant.TenantFilter;
import com.jaasielsilva.erpcorporativo.app.dto.api.admin.tenant.TenantAdminRequest;
import com.jaasielsilva.erpcorporativo.app.dto.api.admin.tenant.TenantRequest;
import com.jaasielsilva.erpcorporativo.app.dto.api.admin.tenant.TenantResponse;
import com.jaasielsilva.erpcorporativo.app.dto.web.admin.AdminTenantCreateForm;
import com.jaasielsilva.erpcorporativo.app.dto.web.admin.AdminTenantListItemViewModel;
import com.jaasielsilva.erpcorporativo.app.dto.web.admin.AdminTenantsPageViewModel;
import com.jaasielsilva.erpcorporativo.app.model.Role;
import com.jaasielsilva.erpcorporativo.app.service.api.v1.admin.TenantAdminApiService;
import com.jaasielsilva.erpcorporativo.app.usecase.api.v1.admin.UsuarioAdminUseCase;
import com.jaasielsilva.erpcorporativo.app.repository.usuario.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminTenantWebService {

    private static final String DEFAULT_ADMIN_RESET_PASSWORD = "mudar123";

    private final TenantAdminApiService tenantAdminApiService;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioAdminUseCase usuarioAdminUseCase;

    public AdminTenantsPageViewModel list(String nome, String slug, Boolean ativo, int page, int size) {
        PageResponse<TenantResponse> response = tenantAdminApiService.list(new TenantFilter(nome, slug, ativo), page, size);
        List<AdminTenantListItemViewModel> tenants = response.content().stream()
                .map(tenant -> new AdminTenantListItemViewModel(
                        tenant.id(),
                        tenant.nome(),
                        tenant.slug(),
                        tenant.ativo(),
                        tenant.totalUsuarios(),
                        tenant.createdAt(),
                        usuarioRepository.findFirstByTenantIdAndRoleOrderByIdAsc(tenant.id(), Role.ADMIN)
                                .map(admin -> admin.getEmail())
                                .orElse("-")
                ))
                .toList();

        return new AdminTenantsPageViewModel(tenants, response.page(), response.totalPages());
    }

    public TenantResponse create(AdminTenantCreateForm form) {
        TenantRequest request = new TenantRequest(
                form.getNome(),
                form.getSlug(),
                form.isAtivo(),
                new TenantAdminRequest(form.getAdminNome(), form.getAdminEmail(), form.getAdminPassword())
        );
        return tenantAdminApiService.create(request);
    }

    public void resetTenantAdminPassword(Long tenantId) {
        usuarioAdminUseCase.resetTenantAdminPassword(tenantId, DEFAULT_ADMIN_RESET_PASSWORD);
    }
}
