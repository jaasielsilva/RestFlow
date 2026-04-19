package com.jaasielsilva.erpcorporativo.app.repository.permission;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.jaasielsilva.erpcorporativo.app.model.AccessLevel;
import com.jaasielsilva.erpcorporativo.app.model.Role;
import com.jaasielsilva.erpcorporativo.app.model.TenantRolePermission;

public interface TenantRolePermissionRepository extends JpaRepository<TenantRolePermission, Long> {

    List<TenantRolePermission> findAllByTenantId(Long tenantId);

    List<TenantRolePermission> findAllByTenantIdAndRole(Long tenantId, Role role);

    Optional<TenantRolePermission> findByTenantIdAndModuleIdAndRole(Long tenantId, Long moduleId, Role role);

    @Query("""
            select coalesce(trp.accessLevel, 'NONE')
            from TenantRolePermission trp
            where trp.tenant.id = :tenantId
              and trp.module.id = :moduleId
              and trp.role = :role
            """)
    Optional<AccessLevel> findAccessLevel(
            @Param("tenantId") Long tenantId,
            @Param("moduleId") Long moduleId,
            @Param("role") Role role
    );
}
