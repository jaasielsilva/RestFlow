package com.jaasielsilva.erpcorporativo.app.service.web.tenantadmin;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.jaasielsilva.erpcorporativo.app.dto.web.tenantadmin.TenantDashboardViewModel;
import com.jaasielsilva.erpcorporativo.app.dto.web.tenantadmin.TenantPortalModuleViewModel;
import com.jaasielsilva.erpcorporativo.app.model.Role;
import com.jaasielsilva.erpcorporativo.app.repository.usuario.UsuarioRepository;
import com.jaasielsilva.erpcorporativo.app.security.AppUserDetails;
import com.jaasielsilva.erpcorporativo.app.security.SecurityPrincipalUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TenantDashboardWebService {

    private final UsuarioRepository usuarioRepository;
    private final TenantPortalWebService tenantPortalWebService;

    public TenantDashboardViewModel build(Authentication authentication) {
        AppUserDetails currentUser = SecurityPrincipalUtils.getCurrentUser(authentication);

        long totalUsuarios = usuarioRepository.countByTenantId(currentUser.getTenantId());
        long usuariosAtivos = usuarioRepository.countByTenantIdAndAtivoTrue(currentUser.getTenantId());
        long adminsAtivos = usuarioRepository.countByTenantIdAndRoleAndAtivoTrue(currentUser.getTenantId(), Role.ADMIN);

        List<TenantPortalModuleViewModel> modules = tenantPortalWebService.listEnabledModules(authentication);
        int modulosHabilitados = Math.max(0, modules.size() - 1);

        return new TenantDashboardViewModel(totalUsuarios, usuariosAtivos, adminsAtivos, modulosHabilitados);
    }
}
