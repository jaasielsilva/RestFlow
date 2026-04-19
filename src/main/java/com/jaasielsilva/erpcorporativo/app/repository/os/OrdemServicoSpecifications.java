package com.jaasielsilva.erpcorporativo.app.repository.os;

import org.springframework.data.jpa.domain.Specification;

import com.jaasielsilva.erpcorporativo.app.model.OrdemServico;
import com.jaasielsilva.erpcorporativo.app.model.OrdemServicoStatus;

public final class OrdemServicoSpecifications {

    private OrdemServicoSpecifications() {}

    public static Specification<OrdemServico> byTenant(Long tenantId) {
        return (root, query, cb) -> cb.equal(root.get("tenant").get("id"), tenantId);
    }

    public static Specification<OrdemServico> byStatus(OrdemServicoStatus status) {
        return (root, query, cb) -> status == null ? cb.conjunction()
                : cb.equal(root.get("status"), status);
    }

    public static Specification<OrdemServico> byCliente(String cliente) {
        return (root, query, cb) -> (cliente == null || cliente.isBlank()) ? cb.conjunction()
                : cb.like(cb.lower(root.get("clienteNome")), "%" + cliente.toLowerCase() + "%");
    }

    public static Specification<OrdemServico> byTitulo(String titulo) {
        return (root, query, cb) -> (titulo == null || titulo.isBlank()) ? cb.conjunction()
                : cb.like(cb.lower(root.get("titulo")), "%" + titulo.toLowerCase() + "%");
    }
}
