package com.jaasielsilva.erpcorporativo.app.repository.module;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.jaasielsilva.erpcorporativo.app.model.TenantModule;

public interface TenantModuleRepository extends JpaRepository<TenantModule, Long> {

    List<TenantModule> findAllByTenantId(Long tenantId);

    @Query("""
            select tm
            from TenantModule tm
            join fetch tm.module m
            where tm.tenant.id = :tenantId
              and tm.ativo = true
              and m.ativo = true
            order by lower(m.nome)
            """)
    List<TenantModule> findEnabledModulesByTenantId(@Param("tenantId") Long tenantId);

    Optional<TenantModule> findByTenantIdAndModuleId(Long tenantId, Long moduleId);

    boolean existsByTenantIdAndModuleCodigoIgnoreCaseAndAtivoTrue(Long tenantId, String codigo);

    @Query("""
            select count(tm) > 0
            from TenantModule tm
            join tm.module m
            where tm.tenant.id = :tenantId
              and lower(m.codigo) = lower(:codigo)
              and tm.ativo = true
              and m.ativo = true
            """)
    boolean hasEnabledModuleByCodigo(@Param("tenantId") Long tenantId, @Param("codigo") String codigo);

    @org.springframework.data.jpa.repository.Modifying
    @Query("delete from TenantModule tm where tm.module.id = :moduleId")
    void deleteByModuleId(@Param("moduleId") Long moduleId);
}
