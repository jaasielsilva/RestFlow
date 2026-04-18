package com.jaasielsilva.erpcorporativo.app.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.jaasielsilva.erpcorporativo.app.config.properties.AppBootstrapProperties;
import com.jaasielsilva.erpcorporativo.app.model.Role;
import com.jaasielsilva.erpcorporativo.app.model.Tenant;
import com.jaasielsilva.erpcorporativo.app.model.Usuario;
import com.jaasielsilva.erpcorporativo.app.repository.tenant.TenantRepository;
import com.jaasielsilva.erpcorporativo.app.repository.usuario.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private DataInitializer dataInitializer;

    @BeforeEach
    void setUp() {
        AppBootstrapProperties properties = new AppBootstrapProperties();
        dataInitializer = new DataInitializer(usuarioRepository, passwordEncoder, tenantRepository, properties);
    }

    @Test
    void deveCriarSuperAdminComSenhaCriptografadaETenantDaPlataforma() throws Exception {
        when(usuarioRepository.countByRole(Role.SUPER_ADMIN)).thenReturn(0L);
        when(tenantRepository.findBySlug("platform")).thenReturn(Optional.empty());
        when(tenantRepository.save(any(Tenant.class))).thenAnswer(invocation -> {
            Tenant tenant = invocation.getArgument(0);
            tenant.setId(1L);
            return tenant;
        });
        when(usuarioRepository.findFirstByEmailIgnoreCase("admin@erpcorporativo.com")).thenReturn(Optional.empty());
        when(usuarioRepository.findFirstByEmailIgnoreCase("admin@admin.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("123456")).thenReturn("$2a$hash");

        dataInitializer.run();

        ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(usuarioCaptor.capture());

        Usuario adminCriado = usuarioCaptor.getValue();
        assertEquals("admin@erpcorporativo.com", adminCriado.getEmail());
        assertEquals("Super Admin", adminCriado.getNome());
        assertEquals(Role.SUPER_ADMIN, adminCriado.getRole());
        assertTrue(adminCriado.isAtivo());
        assertEquals("$2a$hash", adminCriado.getPassword());
        assertNotNull(adminCriado.getTenant());
        assertEquals(1L, adminCriado.getTenant().getId());
    }

    @Test
    void deveMigrarAdminLegadoComSenhaEmTextoParaHashBcrypt() throws Exception {
        Tenant platformTenant = Tenant.builder()
                .id(10L)
                .nome("Plataforma SaaS")
                .slug("platform")
                .ativo(true)
                .build();

        Usuario legacyAdmin = Usuario.builder()
                .id(99L)
                .nome("Admin Antigo")
                .email("admin@admin.com")
                .password("123456")
                .role(Role.ADMIN)
                .ativo(false)
                .build();

        when(usuarioRepository.countByRole(Role.SUPER_ADMIN)).thenReturn(0L);
        when(tenantRepository.findBySlug("platform")).thenReturn(Optional.of(platformTenant));
        when(usuarioRepository.findFirstByEmailIgnoreCase("admin@erpcorporativo.com")).thenReturn(Optional.empty());
        when(usuarioRepository.findFirstByEmailIgnoreCase("admin@admin.com")).thenReturn(Optional.of(legacyAdmin));
        when(passwordEncoder.encode("123456")).thenReturn("$2a$novoHash");

        dataInitializer.run();

        verify(usuarioRepository).save(legacyAdmin);
        assertEquals("admin@erpcorporativo.com", legacyAdmin.getEmail());
        assertEquals(Role.SUPER_ADMIN, legacyAdmin.getRole());
        assertTrue(legacyAdmin.isAtivo());
        assertEquals("$2a$novoHash", legacyAdmin.getPassword());
        assertEquals(platformTenant, legacyAdmin.getTenant());
    }
}
