package com.jaasielsilva.erpcorporativo.app.repository.contract;

import org.springframework.data.jpa.domain.Specification;

import com.jaasielsilva.erpcorporativo.app.model.Contract;
import com.jaasielsilva.erpcorporativo.app.model.ContractStatus;

public final class ContractSpecifications {

    private ContractSpecifications() {}

    public static Specification<Contract> byTenantNome(String nome) {
        return (root, query, cb) -> (nome == null || nome.isBlank()) ? cb.conjunction()
                : cb.like(cb.lower(root.get("tenant").get("nome")), "%" + nome.toLowerCase() + "%");
    }

    public static Specification<Contract> byStatus(ContractStatus status) {
        return (root, query, cb) -> status == null ? cb.conjunction()
                : cb.equal(root.get("status"), status);
    }

    public static Specification<Contract> byPlanoCodigo(String planoCodigo) {
        return (root, query, cb) -> (planoCodigo == null || planoCodigo.isBlank()) ? cb.conjunction()
                : cb.like(cb.lower(root.get("subscriptionPlan").get("codigo")), "%" + planoCodigo.toLowerCase() + "%");
    }
}
