package com.jaasielsilva.erpcorporativo.app.repository.usuario;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.jaasielsilva.erpcorporativo.app.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long>, JpaSpecificationExecutor<Usuario> {

    Optional<Usuario> findByEmailIgnoreCaseAndTenantId(String email, Long tenantId);

    Optional<Usuario> findFirstByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndTenantId(String email, Long tenantId);

    long countByTenantId(Long tenantId);

    long countByTenantIdAndRole(Long tenantId, com.jaasielsilva.erpcorporativo.app.model.Role role);

    long countByRole(com.jaasielsilva.erpcorporativo.app.model.Role role);

    Optional<Usuario> findFirstByTenantIdAndRoleOrderByIdAsc(Long tenantId, com.jaasielsilva.erpcorporativo.app.model.Role role);
}
