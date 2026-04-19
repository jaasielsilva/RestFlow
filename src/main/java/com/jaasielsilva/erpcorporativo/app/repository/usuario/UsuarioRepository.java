package com.jaasielsilva.erpcorporativo.app.repository.usuario;

import java.util.List;
import java.util.Optional;
import com.jaasielsilva.erpcorporativo.app.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.jaasielsilva.erpcorporativo.app.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long>, JpaSpecificationExecutor<Usuario> {

    Optional<Usuario> findByEmailIgnoreCaseAndTenantId(String email, Long tenantId);

    Optional<Usuario> findFirstByEmailIgnoreCase(String email);

    List<Usuario> findAllByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndTenantId(String email, Long tenantId);

    long countByTenantId(Long tenantId);

    long countByTenantIdAndRole(Long tenantId, Role role);

    long countByRole(Role role);

    Optional<Usuario> findFirstByTenantIdAndRoleOrderByIdAsc(Long tenantId, Role role);
}
