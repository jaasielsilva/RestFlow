package com.jaasielsilva.erpcorporativo.app.usecase.api.v1.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.jaasielsilva.erpcorporativo.app.dto.api.admin.tenant.TenantAdminRequest;
import com.jaasielsilva.erpcorporativo.app.dto.api.admin.tenant.TenantRequest;
import com.jaasielsilva.erpcorporativo.app.dto.api.admin.user.UsuarioRequest;
import com.jaasielsilva.erpcorporativo.app.exception.ConflictException;
import com.jaasielsilva.erpcorporativo.app.mapper.api.v1.admin.TenantAdminApiMapper;
import com.jaasielsilva.erpcorporativo.app.mapper.api.v1.admin.UsuarioAdminApiMapper;
import com.jaasielsilva.erpcorporativo.app.model.Role;
import com.jaasielsilva.erpcorporativo.app.model.Tenant;
import com.jaasielsilva.erpcorporativo.app.model.Usuario;
import com.jaasielsilva.erpcorporativo.app.repository.tenant.TenantRepository;
import com.jaasielsilva.erpcorporativo.app.repository.usuario.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class AdminRulesUseCaseTest {

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void deveCriarTenantComAdminInicial() {
        TenantAdminApiMapper mapper = new TenantAdminApiMapper();
        TenantAdminUseCase useCase = new TenantAdminUseCase(tenantRepository, usuarioRepository, mapper, passwordEncoder);

        TenantRequest request = new TenantRequest(
                "Cliente A",
                "cliente-a",
                true,
                new TenantAdminRequest("Admin Cliente", "admin@cliente.com", "Senha123")
        );

        when(tenantRepository.findBySlugIgnoreCase("cliente-a")).thenReturn(Optional.empty());
        when(tenantRepository.save(any(Tenant.class))).thenAnswer(invocation -> {
            Tenant tenant = invocation.getArgument(0);
            tenant.setId(20L);
            return tenant;
        });
        when(passwordEncoder.encode("Senha123")).thenReturn("$2a$tenantAdmin");

        var response = useCase.create(request);

        ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(usuarioCaptor.capture());

        Usuario adminCriado = usuarioCaptor.getValue();
        assertEquals(20L, adminCriado.getTenant().getId());
        assertEquals(Role.ADMIN, adminCriado.getRole());
        assertEquals("$2a$tenantAdmin", adminCriado.getPassword());
        assertEquals(1L, response.totalUsuarios());
    }

    @Test
    void naoDevePermitirRemoverUltimoAdminDoTenant() {
        UsuarioAdminApiMapper mapper = new UsuarioAdminApiMapper();
        UsuarioAdminUseCase useCase = new UsuarioAdminUseCase(
                usuarioRepository,
                tenantRepository,
                mapper,
                passwordEncoder
        );

        Tenant tenant = Tenant.builder().id(30L).nome("Cliente B").slug("cliente-b").ativo(true).build();
        Usuario admin = Usuario.builder()
                .id(100L)
                .nome("Admin")
                .email("admin@cliente-b.com")
                .password("$2a$hash")
                .role(Role.ADMIN)
                .ativo(true)
                .tenant(tenant)
                .build();

        when(usuarioRepository.findById(100L)).thenReturn(Optional.of(admin));
        when(usuarioRepository.countByTenantIdAndRole(30L, Role.ADMIN)).thenReturn(1L);

        assertThrows(ConflictException.class, () -> useCase.delete(100L));
    }

    @Test
    void naoDevePermitirCriarSuperAdminPorFluxoAdministrativo() {
        UsuarioAdminApiMapper mapper = new UsuarioAdminApiMapper();
        UsuarioAdminUseCase useCase = new UsuarioAdminUseCase(
                usuarioRepository,
                tenantRepository,
                mapper,
                passwordEncoder
        );

        Tenant tenant = Tenant.builder().id(40L).nome("Cliente C").slug("cliente-c").ativo(true).build();
        when(tenantRepository.findById(40L)).thenReturn(Optional.of(tenant));

        UsuarioRequest request = new UsuarioRequest(
                "Tentativa",
                "super@cliente.com",
                "Senha123",
                true,
                Role.SUPER_ADMIN,
                40L
        );

        assertThrows(ConflictException.class, () -> useCase.create(request));
    }
}
