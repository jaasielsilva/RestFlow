package com.jaasielsilva.erpcorporativo.app.tenant;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void shouldFilterUsuariosByTenantWhenTenantContextIsSet() {
        int baselineUsers = usuarioRepository.findAll().size();

        Tenant tenantA = tenantRepository.save(Tenant.builder().nome("Empresa A").slug("empresa-a").ativo(true).build());
        Tenant tenantB = tenantRepository.save(Tenant.builder().nome("Empresa B").slug("empresa-b").ativo(true).build());

        usuarioRepository.save(Usuario.builder()
                .email("user-a@empresa.com")
                .password("$2a$hash")
                .nome("User A")
                .ativo(true)
                .role(Role.ADMIN)
                .tenant(tenantA)
                .build());

        usuarioRepository.save(Usuario.builder()
                .email("user-b@empresa.com")
                .password("$2a$hash")
                .nome("User B")
                .ativo(true)
                .role(Role.ADMIN)
                .tenant(tenantB)
                .build());

        TenantContext.setTenantId(tenantA.getId());

        List<Usuario> usuarios = usuarioRepository.findAll();
        assertEquals(1, usuarios.size());
        assertEquals(tenantA.getId(), usuarios.get(0).getTenant().getId());

        TenantContext.clear();

        List<Usuario> todos = usuarioRepository.findAll();
        assertEquals(baselineUsers + 2, todos.size());
    }
}
