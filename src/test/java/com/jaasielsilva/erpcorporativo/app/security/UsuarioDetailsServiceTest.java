package com.jaasielsilva.erpcorporativo.app.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import com.jaasielsilva.erpcorporativo.app.model.Role;
import com.jaasielsilva.erpcorporativo.app.model.Tenant;
import com.jaasielsilva.erpcorporativo.app.model.Usuario;
import com.jaasielsilva.erpcorporativo.app.repository.usuario.UsuarioRepository;
import com.jaasielsilva.erpcorporativo.app.tenant.TenantContext;

@ExtendWith(MockitoExtension.class)
class UsuarioDetailsServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void deveBuscarUsuarioPorTenantQuandoContextoEstiverPreenchido() {
        Usuario usuario = Usuario.builder()
                .id(10L)
                .email("admin@empresa.com")
                .password("$2a$hash")
                .role(Role.ADMIN)
                .ativo(true)
                .tenant(Tenant.builder().id(7L).build())
                .build();

        TenantContext.setTenantId(7L);
        when(usuarioRepository.findByEmailIgnoreCaseAndTenantId("admin@empresa.com", 7L))
                .thenReturn(Optional.of(usuario));

        UserDetails userDetails = new UsuarioDetailsService(usuarioRepository)
                .loadUserByUsername("admin@empresa.com");

        verify(usuarioRepository).findByEmailIgnoreCaseAndTenantId("admin@empresa.com", 7L);
        assertEquals("admin@empresa.com", userDetails.getUsername());
        assertEquals("$2a$hash", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")));
        assertEquals(7L, ((AppUserDetails) userDetails).getTenantId());
    }
}
