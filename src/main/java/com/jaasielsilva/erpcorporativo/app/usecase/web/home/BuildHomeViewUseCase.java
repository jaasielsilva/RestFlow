package com.jaasielsilva.erpcorporativo.app.usecase.web.home;

import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.jaasielsilva.erpcorporativo.app.dto.web.home.HomeViewModel;
import com.jaasielsilva.erpcorporativo.app.mapper.web.home.HomeWebMapper;
import com.jaasielsilva.erpcorporativo.app.repository.tenant.TenantRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BuildHomeViewUseCase {

    private static final int RECENT_TENANTS_LIMIT = 4;

    private final HomeWebMapper homeWebMapper;
    private final TenantRepository tenantRepository;

    public HomeViewModel execute(Authentication authentication) {
        return homeWebMapper.toViewModel(
                authentication.getName(),
                tenantRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, RECENT_TENANTS_LIMIT))
        );
    }
}
