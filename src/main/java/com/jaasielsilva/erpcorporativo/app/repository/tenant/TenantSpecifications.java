package com.jaasielsilva.erpcorporativo.app.repository.tenant;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import com.jaasielsilva.erpcorporativo.app.dto.api.admin.tenant.TenantFilter;
import com.jaasielsilva.erpcorporativo.app.model.Tenant;

public final class TenantSpecifications {

    private TenantSpecifications() {
    }

    public static Specification<Tenant> withFilter(TenantFilter filter) {
        return Specification.allOf(
                hasNome(filter.nome()),
                hasSlug(filter.slug()),
                hasAtivo(filter.ativo())
        );
    }

    private static Specification<Tenant> hasNome(String nome) {
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

    private static Specification<Tenant> hasSlug(String slug) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(slug)) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("slug")),
                    "%" + slug.toLowerCase() + "%"
            );
        };
    }

    private static Specification<Tenant> hasAtivo(Boolean ativo) {
        return (root, query, criteriaBuilder) -> ativo == null
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.equal(root.get("ativo"), ativo);
    }
}
