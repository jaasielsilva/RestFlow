package com.jaasielsilva.erpcorporativo.app.config;

import java.util.Optional;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;

import com.jaasielsilva.erpcorporativo.app.config.properties.AppBootstrapProperties;
import com.jaasielsilva.erpcorporativo.app.model.Role;
import com.jaasielsilva.erpcorporativo.app.model.Tenant;
import com.jaasielsilva.erpcorporativo.app.model.Usuario;
import com.jaasielsilva.erpcorporativo.app.repository.tenant.TenantRepository;
import com.jaasielsilva.erpcorporativo.app.repository.usuario.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TenantRepository tenantRepository;
    private final AppBootstrapProperties bootstrapProperties;

    @Override
    public void run(String... args) {
        Tenant platformTenant = ensurePlatformTenant();
        ensureSuperAdmin(platformTenant);
    }

    private Tenant ensurePlatformTenant() {
        String tenantSlug = bootstrapProperties.getTenant().getSlug();

        return tenantRepository.findBySlug(tenantSlug)
                .orElseGet(() -> tenantRepository.save(Tenant.builder()
                        .nome(bootstrapProperties.getTenant().getNome())
                        .slug(tenantSlug)
                        .ativo(true)
                        .build()));
    }

    private void ensureSuperAdmin(Tenant platformTenant) {
        validateBootstrapSecret();
        validateSingleSuperAdmin();

        String canonicalEmail = bootstrapProperties.getSuperAdmin().getEmail();
        String legacyEmail = "admin@admin.com";

        Optional<Usuario> existingAdmin = usuarioRepository.findFirstByEmailIgnoreCase(canonicalEmail);

        if (existingAdmin.isEmpty()) {
            existingAdmin = usuarioRepository.findFirstByEmailIgnoreCase(legacyEmail);
        }

        Usuario admin = existingAdmin.orElseGet(Usuario::new);
        admin.setNome(bootstrapProperties.getSuperAdmin().getNome());
        admin.setEmail(canonicalEmail);
        admin.setRole(Role.SUPER_ADMIN);
        admin.setAtivo(true);
        admin.setTenant(platformTenant);

        if (!isBcryptHash(admin.getPassword())) {
            admin.setPassword(passwordEncoder.encode(bootstrapProperties.getSuperAdmin().getPassword()));
        }

        boolean novoCadastro = admin.getId() == null;
        usuarioRepository.save(admin);

        if (novoCadastro) {
            System.out.println("SUPER ADMIN criado com senha criptografada e tenant da plataforma.");
            return;
        }

        System.out.println("SUPER ADMIN validado e estrutura multi-tenant inicializada.");
    }

    private boolean isBcryptHash(String password) {
        return password != null && password.startsWith("$2");
    }

    private void validateBootstrapSecret() {
        if (!StringUtils.hasText(bootstrapProperties.getSuperAdmin().getPassword())) {
            throw new IllegalStateException(
                    "Defina BOOTSTRAP_SUPER_ADMIN_PASSWORD ou application-local.yml antes de iniciar a aplicação.");
        }
    }

    private void validateSingleSuperAdmin() {
        long superAdminCount = usuarioRepository.countByRole(Role.SUPER_ADMIN);

        if (superAdminCount > 1) {
            throw new IllegalStateException("A plataforma permite apenas um único SUPER_ADMIN.");
        }
    }
}
