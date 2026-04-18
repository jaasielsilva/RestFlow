package com.jaasielsilva.erpcorporativo.app.mapper.api.v1.admin;

import org.springframework.stereotype.Component;

import com.jaasielsilva.erpcorporativo.app.dto.api.PageResponse;
import com.jaasielsilva.erpcorporativo.app.dto.api.admin.user.UsuarioRequest;
import com.jaasielsilva.erpcorporativo.app.dto.api.admin.user.UsuarioResponse;
import com.jaasielsilva.erpcorporativo.app.model.Tenant;
import com.jaasielsilva.erpcorporativo.app.model.Usuario;

@Component
public class UsuarioAdminApiMapper {

    public Usuario toNewEntity(UsuarioRequest request, Tenant tenant, String encodedPassword) {
        return Usuario.builder()
                .nome(request.nome())
                .email(request.email())
                .password(encodedPassword)
                .ativo(request.ativo())
                .role(request.role())
                .tenant(tenant)
                .build();
    }

    public void updateEntity(Usuario usuario, UsuarioRequest request, Tenant tenant, String encodedPassword) {
        usuario.setNome(request.nome());
        usuario.setEmail(request.email());
        usuario.setAtivo(request.ativo());
        usuario.setRole(request.role());
        usuario.setTenant(tenant);

        if (encodedPassword != null) {
            usuario.setPassword(encodedPassword);
        }
    }

    public UsuarioResponse toResponse(Usuario usuario) {
        Tenant tenant = usuario.getTenant();

        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.isAtivo(),
                usuario.getRole(),
                tenant != null ? tenant.getId() : null,
                tenant != null ? tenant.getNome() : null
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
