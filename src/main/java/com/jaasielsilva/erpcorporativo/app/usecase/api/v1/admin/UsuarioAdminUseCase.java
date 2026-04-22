package com.jaasielsilva.erpcorporativo.app.usecase.api.v1.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.jaasielsilva.erpcorporativo.app.dto.api.PageResponse;
import com.jaasielsilva.erpcorporativo.app.dto.api.admin.user.UsuarioFilter;
import com.jaasielsilva.erpcorporativo.app.dto.api.admin.user.UsuarioRequest;
import com.jaasielsilva.erpcorporativo.app.dto.api.admin.user.UsuarioResponse;
import com.jaasielsilva.erpcorporativo.app.exception.ConflictException;
import com.jaasielsilva.erpcorporativo.app.exception.ResourceNotFoundException;
import com.jaasielsilva.erpcorporativo.app.exception.ValidationException;
import com.jaasielsilva.erpcorporativo.app.mapper.api.v1.admin.UsuarioAdminApiMapper;
import com.jaasielsilva.erpcorporativo.app.model.Role;
import com.jaasielsilva.erpcorporativo.app.model.Tenant;
import com.jaasielsilva.erpcorporativo.app.model.Usuario;
import com.jaasielsilva.erpcorporativo.app.repository.tenant.TenantRepository;
import com.jaasielsilva.erpcorporativo.app.repository.usuario.UsuarioRepository;
import com.jaasielsilva.erpcorporativo.app.repository.usuario.UsuarioSpecifications;
import com.jaasielsilva.erpcorporativo.app.service.shared.PlatformSettingService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UsuarioAdminUseCase {

    private final UsuarioRepository usuarioRepository;
    private final TenantRepository tenantRepository;
    private final UsuarioAdminApiMapper usuarioAdminApiMapper;
    private final PasswordEncoder passwordEncoder;
    private final PlatformSettingService platformSettingService;

    @Transactional(readOnly = true)
    public PageResponse<UsuarioResponse> list(UsuarioFilter filter, int page, int size) {
        Pageable pageable = PageRequest.of(page, normalizeSize(size), Sort.by("nome").ascending());
        Page<UsuarioResponse> responsePage = usuarioRepository.findAll(UsuarioSpecifications.withFilter(filter), pageable)
                .map(usuarioAdminApiMapper::toResponse);
        return usuarioAdminApiMapper.toPageResponse(responsePage);
    }

    @Transactional(readOnly = true)
    public PageResponse<UsuarioResponse> listByTenant(Long tenantId, UsuarioFilter filter, int page, int size) {
        UsuarioFilter tenantScopedFilter = new UsuarioFilter(
                filter.nome(),
                filter.email(),
                tenantId,
                filter.ativo(),
                filter.role()
        );
        return list(tenantScopedFilter, page, size);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse getById(Long id) {
        return usuarioAdminApiMapper.toResponse(findUsuario(id));
    }

    @Transactional(readOnly = true)
    public UsuarioResponse getByIdForTenant(Long tenantId, Long id) {
        Usuario usuario = findUsuario(id);

        if (usuario.getTenant() == null || !tenantId.equals(usuario.getTenant().getId())) {
            throw new ResourceNotFoundException("Usuário não encontrado no tenant informado: " + id);
        }

        return usuarioAdminApiMapper.toResponse(usuario);
    }

    @Transactional
    public UsuarioResponse create(UsuarioRequest request) {
        Tenant tenant = resolveTenant(request.tenantId());
        validateRoleRulesForCreate(request, tenant);
        validateEmail(request.email(), tenant != null ? tenant.getId() : null, null);
        String encodedPassword = encodeRequiredPassword(request.password());

        Usuario usuario = usuarioAdminApiMapper.toNewEntity(request, tenant, encodedPassword);
        Usuario saved = usuarioRepository.save(usuario);
        return usuarioAdminApiMapper.toResponse(saved);
    }

    @Transactional
    public UsuarioResponse createForTenantAdmin(Long tenantId, UsuarioRequest request) {
        validateTenantAdminScope(request, tenantId);
        UsuarioRequest normalizedRequest = new UsuarioRequest(
                request.nome(),
                request.email(),
                request.password(),
                request.ativo(),
                request.role(),
                tenantId
        );
        return create(normalizedRequest);
    }

    @Transactional
    public UsuarioResponse update(Long id, UsuarioRequest request) {
        Usuario usuario = findUsuario(id);
        Tenant tenant = resolveTenant(request.tenantId());
        validateRoleRulesForUpdate(usuario, request, tenant);
        validateEmail(request.email(), tenant != null ? tenant.getId() : null, id);
        String encodedPassword = encodeOptionalPassword(request.password());

        usuarioAdminApiMapper.updateEntity(usuario, request, tenant, encodedPassword);
        Usuario saved = usuarioRepository.save(usuario);
        return usuarioAdminApiMapper.toResponse(saved);
    }

    @Transactional
    public UsuarioResponse updateForTenantAdmin(Long tenantId, Long id, UsuarioRequest request) {
        Usuario usuario = findUsuario(id);

        if (usuario.getTenant() == null || !tenantId.equals(usuario.getTenant().getId())) {
            throw new ResourceNotFoundException("Usuário não encontrado no tenant informado: " + id);
        }

        validateTenantAdminScope(request, tenantId);
        UsuarioRequest normalizedRequest = new UsuarioRequest(
                request.nome(),
                request.email(),
                request.password(),
                request.ativo(),
                request.role(),
                tenantId
        );
        return update(id, normalizedRequest);
    }

    @Transactional
    public void delete(Long id) {
        Usuario usuario = findUsuario(id);

        if (usuario.getRole() == Role.SUPER_ADMIN) {
            throw new ConflictException("O SUPER ADMIN não pode ser removido.");
        }

        ensureTenantWillKeepAdmin(usuario);

        usuarioRepository.delete(usuario);
    }

    @Transactional
    public void deleteForTenantAdmin(Long tenantId, Long id) {
        Usuario usuario = findUsuario(id);

        if (usuario.getTenant() == null || !tenantId.equals(usuario.getTenant().getId())) {
            throw new ResourceNotFoundException("Usuário não encontrado no tenant informado: " + id);
        }

        delete(id);
    }

    @Transactional
    public void resetTenantAdminPassword(Long tenantId, String newPassword) {
        Usuario tenantAdmin = usuarioRepository.findFirstByTenantIdAndRoleOrderByIdAsc(tenantId, Role.ADMIN)
                .orElseThrow(() -> new ResourceNotFoundException("ADMIN do tenant não encontrado: " + tenantId));

        if (!StringUtils.hasText(newPassword)) {
            throw new ValidationException("Senha de reset é obrigatória.");
        }
        tenantAdmin.setPassword(passwordEncoder.encode(newPassword));
        tenantAdmin.setAtivo(true);
        usuarioRepository.save(tenantAdmin);
    }

    @Transactional
    public void resetUserPasswordForTenantAdmin(Long tenantId, Long userId, String newPassword) {
        if (!StringUtils.hasText(newPassword)) {
            throw new ValidationException("Senha de reset é obrigatória.");
        }

        Usuario usuario = findUsuario(userId);
        if (usuario.getTenant() == null || !tenantId.equals(usuario.getTenant().getId())) {
            throw new ResourceNotFoundException("Usuário não encontrado no tenant informado: " + userId);
        }

        usuario.setPassword(passwordEncoder.encode(newPassword));
        usuario.setAtivo(true);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void resetUserPassword(Long userId, String newPassword) {
        if (!StringUtils.hasText(newPassword)) {
            throw new ValidationException("Senha de reset é obrigatória.");
        }

        Usuario usuario = findUsuario(userId);
        if (usuario.getRole() == Role.SUPER_ADMIN) {
            throw new ConflictException("O SUPER ADMIN não pode ter a senha resetada por este fluxo.");
        }
        usuario.setPassword(passwordEncoder.encode(newPassword));
        usuario.setAtivo(true);
        usuarioRepository.save(usuario);
    }

    private Usuario findUsuario(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + id));
    }

    private Tenant resolveTenant(Long tenantId) {
        if (tenantId == null) {
            return null;
        }

        return tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant não encontrado: " + tenantId));
    }

    private void validateEmail(String email, Long tenantId, Long usuarioId) {
        if (tenantId == null) {
            throw new ValidationException("tenantId é obrigatório para usuários administrativos e operacionais.");
        }

        usuarioRepository.findByEmailIgnoreCaseAndTenantId(email, tenantId)
                .filter(existing -> !existing.getId().equals(usuarioId))
                .ifPresent(existing -> {
                    throw new ConflictException("Já existe um usuário com este email no tenant informado.");
                });
    }

    private void validateRoleRulesForCreate(UsuarioRequest request, Tenant tenant) {
        if (request.role() == Role.SUPER_ADMIN) {
            throw new ConflictException("O SUPER_ADMIN é único e não pode ser criado por este fluxo.");
        }

        if (tenant == null) {
            throw new ValidationException("tenantId é obrigatório para criação de usuário.");
        }
    }

    private void validateTenantAdminScope(UsuarioRequest request, Long tenantId) {
        if (request.role() == Role.SUPER_ADMIN) {
            throw new ConflictException("O ADMIN do tenant não pode criar ou promover SUPER_ADMIN.");
        }

        if (request.tenantId() != null && !tenantId.equals(request.tenantId())) {
            throw new ConflictException("O ADMIN do tenant só pode operar dentro do próprio tenant.");
        }
    }

    private void validateRoleRulesForUpdate(Usuario usuarioAtual, UsuarioRequest request, Tenant tenant) {
        if (usuarioAtual.getRole() == Role.SUPER_ADMIN && request.role() != Role.SUPER_ADMIN) {
            throw new ConflictException("O SUPER_ADMIN único da plataforma não pode ser rebaixado por este fluxo.");
        }

        if (request.role() == Role.SUPER_ADMIN && usuarioAtual.getRole() != Role.SUPER_ADMIN) {
            throw new ConflictException("Não é permitido promover usuário para SUPER_ADMIN por este fluxo.");
        }

        if (tenant == null) {
            throw new ValidationException("tenantId é obrigatório para atualização de usuário.");
        }

        if (usuarioAtual.getRole() == Role.ADMIN
                && request.role() != Role.ADMIN
                && usuarioRepository.countByTenantIdAndRole(usuarioAtual.getTenant().getId(), Role.ADMIN) <= 1) {
            throw new ConflictException("O tenant não pode ficar sem nenhum ADMIN.");
        }

        ensureTenantWillKeepActiveAdmin(usuarioAtual, request);
    }

    private void ensureTenantWillKeepAdmin(Usuario usuario) {
        if (usuario.getRole() != Role.ADMIN) {
            return;
        }

        Long tenantId = usuario.getTenant() != null ? usuario.getTenant().getId() : null;

        if (tenantId != null
                && usuario.isAtivo()
                && usuarioRepository.countByTenantIdAndRoleAndAtivoTrue(tenantId, Role.ADMIN) <= 1) {
            throw new ConflictException("O tenant não pode ficar sem nenhum ADMIN.");
        }
    }

    private void ensureTenantWillKeepActiveAdmin(Usuario usuarioAtual, UsuarioRequest request) {
        if (usuarioAtual.getRole() != Role.ADMIN || !usuarioAtual.isAtivo()) {
            return;
        }

        boolean willRemainActiveAdmin = request.role() == Role.ADMIN && request.ativo();
        if (willRemainActiveAdmin) {
            return;
        }

        Long tenantId = usuarioAtual.getTenant() != null ? usuarioAtual.getTenant().getId() : null;
        if (tenantId == null) {
            return;
        }

        if (usuarioRepository.countByTenantIdAndRoleAndAtivoTrue(tenantId, Role.ADMIN) <= 1) {
            throw new ConflictException("Não é permitido desativar ou rebaixar o único ADMIN ativo da empresa.");
        }
    }

    private String encodeRequiredPassword(String password) {
        if (!StringUtils.hasText(password)) {
            throw new ValidationException("Senha é obrigatória para criação de usuário.");
        }
        validatePasswordPolicy(password);

        return passwordEncoder.encode(password);
    }

    private String encodeOptionalPassword(String password) {
        if (!StringUtils.hasText(password)) {
            return null;
        }
        validatePasswordPolicy(password);

        return passwordEncoder.encode(password);
    }

    private void validatePasswordPolicy(String password) {
        int minLength = 8;
        try {
            minLength = Integer.parseInt(platformSettingService.get(PlatformSettingService.PASSWORD_MIN_LEN, "8"));
        } catch (NumberFormatException ignored) {
            minLength = 8;
        }
        if (password.length() < minLength) {
            throw new ValidationException("Senha deve possuir no mínimo " + minLength + " caracteres.");
        }
    }

    private int normalizeSize(int size) {
        if (size <= 0) {
            return 20;
        }

        return Math.min(size, 100);
    }
}
