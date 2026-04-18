package com.jaasielsilva.erpcorporativo.app.mapper.api.v1.admin;

import org.springframework.stereotype.Component;

import com.jaasielsilva.erpcorporativo.app.dto.api.PageResponse;
import com.jaasielsilva.erpcorporativo.app.dto.api.admin.tenant.TenantRequest;
import com.jaasielsilva.erpcorporativo.app.dto.api.admin.tenant.TenantResponse;
import com.jaasielsilva.erpcorporativo.app.model.Tenant;

@Component
public class TenantAdminApiMapper {

    public Tenant toNewEntity(TenantRequest request) {
        return Tenant.builder()
                .nome(request.nome())
                .slug(request.slug())
                .ativo(request.ativo())
                .build();
    }

    public void updateEntity(Tenant tenant, TenantRequest request) {
        tenant.setNome(request.nome());
        tenant.setSlug(request.slug());
        tenant.setAtivo(request.ativo());
    }

    public TenantResponse toResponse(Tenant tenant, long totalUsuarios) {
        return new TenantResponse(
                tenant.getId(),
                tenant.getNome(),
                tenant.getSlug(),
                tenant.isAtivo(),
                totalUsuarios,
                tenant.getCreatedAt()
        );
    }

    public <T> PageResponse<T> toPageResponse(org.springframework.data.domain.Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
