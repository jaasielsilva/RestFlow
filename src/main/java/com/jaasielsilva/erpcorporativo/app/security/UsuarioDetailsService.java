package com.jaasielsilva.erpcorporativo.app.security;

import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

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

        return usuarioRepository.findFirstByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));
    }
}
