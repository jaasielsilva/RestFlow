package com.jaasielsilva.erpcorporativo.app.security;

import java.util.Collections;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.jaasielsilva.erpcorporativo.app.model.Role;
import com.jaasielsilva.erpcorporativo.app.model.Usuario;
import com.jaasielsilva.erpcorporativo.app.repository.usuario.UsuarioRepository;
import com.jaasielsilva.erpcorporativo.app.tenant.TenantContext;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Long tenantId = TenantContext.getTenantId();

        Usuario usuario = resolveUsuario(email, tenantId);

        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + usuario.getRole().name());

        return new AppUserDetails(
                usuario.getId(),
                usuario.getTenant() != null ? usuario.getTenant().getId() : null,
                usuario.getRole(),
                usuario.getEmail(),
                usuario.getPassword(),
                Collections.singleton(authority)
        );
    }

    private Usuario resolveUsuario(String email, Long tenantId) {
        if (tenantId != null) {
            return usuarioRepository.findByEmailIgnoreCaseAndTenantId(email, tenantId)
                    .orElseThrow(() -> new UsernameNotFoundException(
                            "Usuário não encontrado para o tenant informado: " + email));
        }

        List<Usuario> usuarios = usuarioRepository.findAllByEmailIgnoreCase(email);

        if (usuarios.isEmpty()) {
            throw new UsernameNotFoundException("Usuário não encontrado: " + email);
        }

        // SUPER_ADMIN não tem tenant — resolve direto
        for (Usuario u : usuarios) {
            if (u.getRole() == Role.SUPER_ADMIN) {
                return u;
            }
        }

        // Email vinculado a exatamente um tenant — resolve automaticamente
        if (usuarios.size() == 1) {
            return usuarios.get(0);
        }

        // Múltiplos tenants com o mesmo email — não é possível resolver sem tenantId
        throw new UsernameNotFoundException(
                "Email vinculado a múltiplos tenants. Informe o tenantId via header X-Tenant-Id.");
    }
}
