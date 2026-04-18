package com.jaasielsilva.erpcorporativo.app.repository.usuario;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import com.jaasielsilva.erpcorporativo.app.dto.api.admin.user.UsuarioFilter;
import com.jaasielsilva.erpcorporativo.app.model.Usuario;

public final class UsuarioSpecifications {

    private UsuarioSpecifications() {
    }

    public static Specification<Usuario> withFilter(UsuarioFilter filter) {
        return Specification.allOf(
                hasNome(filter.nome()),
                hasEmail(filter.email()),
                hasTenantId(filter.tenantId()),
                hasAtivo(filter.ativo()),
                hasRole(filter.role())
        );
    }

    private static Specification<Usuario> hasNome(String nome) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(nome)) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("nome")),
                    "%" + nome.toLowerCase() + "%"
            );
        };
    }

    private static Specification<Usuario> hasEmail(String email) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(email)) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("email")),
                    "%" + email.toLowerCase() + "%"
            );
        };
    }

    private static Specification<Usuario> hasTenantId(Long tenantId) {
        return (root, query, criteriaBuilder) -> tenantId == null
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.equal(root.get("tenant").get("id"), tenantId);
    }

    private static Specification<Usuario> hasAtivo(Boolean ativo) {
        return (root, query, criteriaBuilder) -> ativo == null
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.equal(root.get("ativo"), ativo);
    }

    private static Specification<Usuario> hasRole(com.jaasielsilva.erpcorporativo.app.model.Role role) {
        return (root, query, criteriaBuilder) -> role == null
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.equal(root.get("role"), role);
    }
}
