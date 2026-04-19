package com.jaasielsilva.erpcorporativo.app.tenant;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class TenantHibernateFilterAspect {

    @PersistenceContext
    private EntityManager entityManager;

    @Around("execution(* com.jaasielsilva.erpcorporativo.app.repository..*.*(..))")
    public Object applyTenantFilter(ProceedingJoinPoint joinPoint) throws Throwable {
        Long tenantId = TenantContext.getTenantId();

        if (tenantId == null) {
            return joinPoint.proceed();
        }

        Session session = entityManager.unwrap(Session.class);
        boolean alreadyEnabled = session.getEnabledFilter("tenantFilter") != null;
        Filter filter = alreadyEnabled ? session.getEnabledFilter("tenantFilter") : session.enableFilter("tenantFilter");
        filter.setParameter("tenantId", tenantId);

        try {
            return joinPoint.proceed();
        } finally {
            if (!alreadyEnabled) {
                session.disableFilter("tenantFilter");
            }
        }
    }
}
