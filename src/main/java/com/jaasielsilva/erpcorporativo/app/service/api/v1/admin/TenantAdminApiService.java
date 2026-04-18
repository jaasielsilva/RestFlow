package com.jaasielsilva.erpcorporativo.app.service.api.v1.admin;

import org.springframework.stereotype.Service;

import com.jaasielsilva.erpcorporativo.app.dto.api.PageResponse;
import com.jaasielsilva.erpcorporativo.app.dto.api.admin.tenant.TenantFilter;
import com.jaasielsilva.erpcorporativo.app.dto.api.admin.tenant.TenantRequest;
import com.jaasielsilva.erpcorporativo.app.dto.api.admin.tenant.TenantResponse;
import com.jaasielsilva.erpcorporativo.app.usecase.api.v1.admin.TenantAdminUseCase;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TenantAdminApiService {

    private final TenantAdminUseCase tenantAdminUseCase;

    public PageResponse<TenantResponse> list(TenantFilter filter, int page, int size) {
        return tenantAdminUseCase.list(filter, page, size);
    }

    public TenantResponse getById(Long id) {
        return tenantAdminUseCase.getById(id);
    }

    public TenantResponse create(TenantRequest request) {
        return tenantAdminUseCase.create(request);
    }

    public TenantResponse update(Long id, TenantRequest request) {
        return tenantAdminUseCase.update(id, request);
    }

    public void delete(Long id) {
        tenantAdminUseCase.delete(id);
    }
}
