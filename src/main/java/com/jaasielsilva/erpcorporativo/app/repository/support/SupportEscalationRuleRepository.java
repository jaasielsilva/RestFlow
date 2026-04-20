package com.jaasielsilva.erpcorporativo.app.repository.support;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jaasielsilva.erpcorporativo.app.model.SupportEscalationRule;

public interface SupportEscalationRuleRepository extends JpaRepository<SupportEscalationRule, Long> {

    List<SupportEscalationRule> findAllByTenantIdAndAtivoTrue(Long tenantId);
}
