package com.jaasielsilva.erpcorporativo.app.service.web.admin;

import org.springframework.stereotype.Service;

import com.jaasielsilva.erpcorporativo.app.dto.api.PageResponse;
import com.jaasielsilva.erpcorporativo.app.dto.api.admin.user.UsuarioFilter;
import com.jaasielsilva.erpcorporativo.app.dto.api.admin.user.UsuarioResponse;
import com.jaasielsilva.erpcorporativo.app.dto.web.admin.AdminUsersPageViewModel;
import com.jaasielsilva.erpcorporativo.app.model.Role;
import com.jaasielsilva.erpcorporativo.app.repository.usuario.UsuarioRepository;
import com.jaasielsilva.erpcorporativo.app.service.api.v1.admin.UsuarioAdminApiService;
import com.jaasielsilva.erpcorporativo.app.usecase.api.v1.admin.UsuarioAdminUseCase;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminUserWebService {

    private static final String DEFAULT_USER_RESET_PASSWORD = "mudar123";

    private final UsuarioAdminApiService usuarioAdminApiService;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioAdminUseCase usuarioAdminUseCase;

    public AdminUsersPageViewModel list(
            String nome,
            String email,
            Long tenantId,
            Boolean ativo,
            Role role,
            int page,
            int size
    ) {
        PageResponse<UsuarioResponse> response = usuarioAdminApiService.list(
                new UsuarioFilter(nome, email, tenantId, ativo, role),
                page,
                size
        );
        return new AdminUsersPageViewModel(response.content(), response.page(), response.totalPages());
    }

    public UserResetResult resetPassword(Long userId) {
        String userEmail = usuarioRepository.findById(userId)
                .map(usuario -> usuario.getEmail())
                .orElse("-");
        usuarioAdminUseCase.resetUserPassword(userId, DEFAULT_USER_RESET_PASSWORD);
        return new UserResetResult(userEmail, DEFAULT_USER_RESET_PASSWORD);
    }

    public record UserResetResult(String userEmail, String generatedPassword) {
    }
}
