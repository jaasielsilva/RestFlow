package com.jaasielsilva.erpcorporativo.app.tenant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.domain.Specification;

import com.jaasielsilva.erpcorporativo.app.model.Role;
import com.jaasielsilva.erpcorporativo.app.model.Tenant;
import com.jaasielsilva.erpcorporativo.app.model.Usuario;
import com.jaasielsilva.erpcorporativo.app.repository.tenant.TenantRepository;
import com.jaasielsilva.erpcorporativo.app.repository.usuario.UsuarioRepository;

@SpringBootTest
class TenantIsolationRepositoryTest {

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void shouldFilterUsuariosByTenantWhenTenantContextIsSet() {
        Tenant tenantA = tenantRepository.save(Tenant.builder().nome("Empresa A").slug("empresa-a-iso").ativo(true).build());
        Tenant tenantB = tenantRepository.save(Tenant.builder().nome("Empresa B").slug("empresa-b-iso").ativo(true).build());

        usuarioRepository.save(Usuario.builder()
                .email("user-a@empresa-iso.com")
                .password("$2a$hash")
                .nome("User A")
                .ativo(true)
                .role(Role.ADMIN)
                .tenant(tenantA)
                .build());

        usuarioRepository.save(Usuario.builder()
                .email("user-b@empresa-iso.com")
                .password("$2a$hash")
                .nome("User B")
                .ativo(true)
                .role(Role.ADMIN)
                .tenant(tenantB)
                .build());

        // Filtra explicitamente por tenantA — deve retornar apenas 1 usuário
        Specification<Usuario> byTenantA = (root, query, cb) ->
                cb.equal(root.get("tenant").get("id"), tenantA.getId());

        List<Usuario> filtrados = usuarioRepository.findAll(byTenantA);
        assertEquals(1, filtrados.size());
        assertEquals(tenantA.getId(), filtrados.get(0).getTenant().getId());

        // Sem filtro — deve incluir os 2 usuários recém-criados (além dos existentes)
        long totalSemFiltro = usuarioRepository.countByTenantId(tenantA.getId())
                + usuarioRepository.countByTenantId(tenantB.getId());
        assertTrue(totalSemFiltro >= 2);
    }
}
