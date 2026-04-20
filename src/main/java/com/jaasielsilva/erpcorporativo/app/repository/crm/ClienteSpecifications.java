package com.jaasielsilva.erpcorporativo.app.repository.crm;

import com.jaasielsilva.erpcorporativo.app.model.Cliente;
import com.jaasielsilva.erpcorporativo.app.model.StatusCliente;
import com.jaasielsilva.erpcorporativo.app.model.TipoCliente;
import org.springframework.data.jpa.domain.Specification;

public final class ClienteSpecifications {

    private ClienteSpecifications() {}

    public static Specification<Cliente> byTenant(Long tenantId) {
        return (root, query, cb) -> cb.equal(root.get("tenant").get("id"), tenantId);
    }

    public static Specification<Cliente> byNome(String nome) {
        return (root, query, cb) -> (nome == null || nome.isBlank()) ? cb.conjunction()
                : cb.like(cb.lower(root.get("nome")), "%" + nome.toLowerCase() + "%");
    }

    public static Specification<Cliente> byTipo(TipoCliente tipo) {
        return (root, query, cb) -> tipo == null ? cb.conjunction()
                : cb.equal(root.get("tipo"), tipo);
    }

    public static Specification<Cliente> byStatus(StatusCliente status) {
        return (root, query, cb) -> status == null ? cb.conjunction()
                : cb.equal(root.get("status"), status);
    }

    public static Specification<Cliente> byDocumento(String documento) {
        return (root, query, cb) -> (documento == null || documento.isBlank()) ? cb.conjunction()
                : cb.like(root.get("documento"), "%" + documento + "%");
    }
}
