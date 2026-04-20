package com.jaasielsilva.erpcorporativo.app.repository.support;

import java.time.LocalDateTime;

import org.springframework.data.jpa.domain.Specification;

import com.jaasielsilva.erpcorporativo.app.model.SupportSlaState;
import com.jaasielsilva.erpcorporativo.app.model.SupportTicket;
import com.jaasielsilva.erpcorporativo.app.model.SupportTicketPriority;
import com.jaasielsilva.erpcorporativo.app.model.SupportTicketStatus;

public final class SupportTicketSpecifications {

    private SupportTicketSpecifications() {
    }

    public static Specification<SupportTicket> byTenant(Long tenantId) {
        return (root, query, cb) -> cb.equal(root.get("tenant").get("id"), tenantId);
    }

    public static Specification<SupportTicket> byAssunto(String assunto) {
        return (root, query, cb) -> (assunto == null || assunto.isBlank()) ? cb.conjunction()
                : cb.like(cb.lower(root.get("assunto")), "%" + assunto.toLowerCase() + "%");
    }

    public static Specification<SupportTicket> byStatus(SupportTicketStatus status) {
        return (root, query, cb) -> status == null ? cb.conjunction()
                : cb.equal(root.get("status"), status);
    }

    public static Specification<SupportTicket> byPrioridade(SupportTicketPriority prioridade) {
        return (root, query, cb) -> prioridade == null ? cb.conjunction()
                : cb.equal(root.get("prioridade"), prioridade);
    }

    public static Specification<SupportTicket> byCategoria(String categoria) {
        return (root, query, cb) -> (categoria == null || categoria.isBlank()) ? cb.conjunction()
                : cb.like(cb.lower(root.get("categoria")), "%" + categoria.toLowerCase() + "%");
    }

    public static Specification<SupportTicket> byResponsavel(Long responsavelId) {
        return (root, query, cb) -> responsavelId == null ? cb.conjunction()
                : cb.equal(root.get("responsavel").get("id"), responsavelId);
    }

    public static Specification<SupportTicket> byCliente(Long clienteId) {
        return (root, query, cb) -> clienteId == null ? cb.conjunction()
                : cb.equal(root.get("cliente").get("id"), clienteId);
    }

    public static Specification<SupportTicket> bySlaState(SupportSlaState slaState) {
        return (root, query, cb) -> slaState == null ? cb.conjunction()
                : cb.equal(root.get("slaState"), slaState);
    }

    public static Specification<SupportTicket> createdBetween(LocalDateTime de, LocalDateTime ate) {
        return (root, query, cb) -> {
            if (de == null && ate == null) {
                return cb.conjunction();
            }
            if (de != null && ate != null) {
                return cb.between(root.get("createdAt"), de, ate);
            }
            if (de != null) {
                return cb.greaterThanOrEqualTo(root.get("createdAt"), de);
            }
            return cb.lessThanOrEqualTo(root.get("createdAt"), ate);
        };
    }
}
