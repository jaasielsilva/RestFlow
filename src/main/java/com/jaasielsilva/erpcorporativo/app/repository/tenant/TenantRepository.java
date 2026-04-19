package com.jaasielsilva.erpcorporativo.app.repository.tenant;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.domain.Pageable;

import com.jaasielsilva.erpcorporativo.app.model.Tenant;

public interface TenantRepository extends JpaRepository<Tenant, Long>, JpaSpecificationExecutor<Tenant> {

    Optional<Tenant> findBySlug(String slug);

    Optional<Tenant> findBySlugIgnoreCase(String slug);

    boolean existsBySlugIgnoreCase(String slug);

    List<Tenant> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
