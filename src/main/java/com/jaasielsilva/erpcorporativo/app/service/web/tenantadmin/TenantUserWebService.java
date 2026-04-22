package com.jaasielsilva.erpcorporativo.app.service.web.tenantadmin;

import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.jaasielsilva.erpcorporativo.app.dto.api.PageResponse;
import com.jaasielsilva.erpcorporativo.app.dto.api.admin.user.UsuarioFilter;
import com.jaasielsilva.erpcorporativo.app.dto.api.admin.user.UsuarioRequest;
import com.jaasielsilva.erpcorporativo.app.dto.api.admin.user.UsuarioResponse;
import com.jaasielsilva.erpcorporativo.app.dto.web.tenantadmin.TenantPasswordChangeForm;
import com.jaasielsilva.erpcorporativo.app.dto.web.tenantadmin.TenantUserForm;
import com.jaasielsilva.erpcorporativo.app.exception.ConflictException;
import com.jaasielsilva.erpcorporativo.app.exception.ValidationException;
import com.jaasielsilva.erpcorporativo.app.dto.web.tenantadmin.TenantUsersPageViewModel;
import com.jaasielsilva.erpcorporativo.app.model.Role;
import com.jaasielsilva.erpcorporativo.app.repository.usuario.UsuarioRepository;
import com.jaasielsilva.erpcorporativo.app.security.SecurityPrincipalUtils;
import com.jaasielsilva.erpcorporativo.app.service.api.v1.tenantadmin.TenantAdminUserApiService;
import com.jaasielsilva.erpcorporativo.app.service.shared.PlatformSettingService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TenantUserWebService {

    private static final String DEFAULT_RESET_PASSWORD = "mudar123";

    private final TenantAdminUserApiService tenantAdminUserApiService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final PlatformSettingService platformSettingService;

    public TenantUsersPageViewModel list(
            Authentication authentication,
            String nome,
            String email,
            Boolean ativo,
            Role role,
            int page,
            int size
    ) {
        PageResponse<UsuarioResponse> response = tenantAdminUserApiService.list(
                authentication,
                new UsuarioFilter(nome, email, null, ativo, role),
                page,
                size
        );

        return new TenantUsersPageViewModel(response.content(), response.page(), response.totalPages());
    }

    public UsuarioResponse getById(Authentication authentication, Long id) {
        return tenantAdminUserApiService.getById(authentication, id);
    }

    public UsuarioResponse create(Authentication authentication, TenantUserForm form) {
        validateRoleAssignment(authentication, form.getRole());
        return tenantAdminUserApiService.create(authentication, toRequest(form));
    }

    public UsuarioResponse update(Authentication authentication, Long id, TenantUserForm form) {
        validateRoleAssignment(authentication, form.getRole());
        return tenantAdminUserApiService.update(authentication, id, toRequest(form));
    }

    public UsuarioResponse toggleActive(Authentication authentication, Long id) {
        UsuarioResponse current = tenantAdminUserApiService.getById(authentication, id);
        UsuarioRequest request = new UsuarioRequest(
                current.nome(),
                current.email(),
                null,
                !current.ativo(),
                current.role(),
                current.tenantId()
        );
        return tenantAdminUserApiService.update(authentication, id, request);
    }

    public void resetPassword(Authentication authentication, Long id) {
        tenantAdminUserApiService.resetPassword(authentication, id, DEFAULT_RESET_PASSWORD);
    }

    public void changeOwnPassword(Authentication authentication, TenantPasswordChangeForm form) {
        if (!StringUtils.hasText(form.getCurrentPassword())) {
            throw new ValidationException("Informe sua senha atual.");
        }
        if (!StringUtils.hasText(form.getNewPassword())) {
            throw new ValidationException("Informe a nova senha.");
        }
        if (!StringUtils.hasText(form.getConfirmPassword())) {
            throw new ValidationException("Confirme a nova senha.");
        }
        if (!form.getNewPassword().equals(form.getConfirmPassword())) {
            throw new ValidationException("A confirmação da nova senha não confere.");
        }
        if (form.getCurrentPassword().equals(form.getNewPassword())) {
            throw new ValidationException("A nova senha deve ser diferente da senha atual.");
        }

        int minLength = resolvePasswordMinLength();
        if (form.getNewPassword().length() < minLength) {
            throw new ValidationException("Senha deve possuir no mínimo " + minLength + " caracteres.");
        }

        var currentUser = SecurityPrincipalUtils.getCurrentUser(authentication);
        var usuario = usuarioRepository.findById(currentUser.getUsuarioId())
                .orElseThrow(() -> new ValidationException("Usuário autenticado não encontrado."));

        if (!passwordEncoder.matches(form.getCurrentPassword(), usuario.getPassword())) {
            throw new ValidationException("Senha atual inválida.");
        }

        usuario.setPassword(passwordEncoder.encode(form.getNewPassword()));
        usuarioRepository.save(usuario);
    }

    public int resolvePasswordMinLength() {
        try {
            return Integer.parseInt(
                    platformSettingService.get(PlatformSettingService.PASSWORD_MIN_LEN, "8")
            );
        } catch (NumberFormatException ignored) {
            return 8;
        }
    }

    private void validateRoleAssignment(Authentication authentication, Role targetRole) {
        var currentUser = SecurityPrincipalUtils.getCurrentUser(authentication);
        if (currentUser.getRole() == Role.USER && targetRole == Role.ADMIN) {
            throw new ConflictException("Usuário USER não pode criar ou promover para ADMIN.");
        }
    }

    private UsuarioRequest toRequest(TenantUserForm form) {
        return new UsuarioRequest(
                form.getNome(),
                form.getEmail(),
                form.getPassword(),
                form.isAtivo(),
                form.getRole(),
                null
        );
    }

}
