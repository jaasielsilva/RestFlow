package com.jaasielsilva.erpcorporativo.app.usecase.api.v1.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.jaasielsilva.erpcorporativo.app.dto.api.PageResponse;
import com.jaasielsilva.erpcorporativo.app.dto.api.admin.tenant.TenantAdminRequest;
import com.jaasielsilva.erpcorporativo.app.dto.api.admin.tenant.TenantFilter;
import com.jaasielsilva.erpcorporativo.app.dto.api.admin.tenant.TenantRequest;
import com.jaasielsilva.erpcorporativo.app.dto.api.admin.tenant.TenantResponse;
import com.jaasielsilva.erpcorporativo.app.exception.ConflictException;
import com.jaasielsilva.erpcorporativo.app.exception.ResourceNotFoundException;
import com.jaasielsilva.erpcorporativo.app.mapper.api.v1.admin.TenantAdminApiMapper;
import com.jaasielsilva.erpcorporativo.app.model.Role;
import com.jaasielsilva.erpcorporativo.app.model.Tenant;
import com.jaasielsilva.erpcorporativo.app.model.Usuario;
import com.jaasielsilva.erpcorporativo.app.repository.tenant.TenantRepository;
import com.jaasielsilva.erpcorporativo.app.repository.tenant.TenantSpecifications;
import com.jaasielsilva.erpcorporativo.app.repository.usuario.UsuarioRepository;

import com.jaasielsilva.erpcorporativo.app.service.shared.AuditService;
import com.jaasielsilva.erpcorporativo.app.model.AuditAction;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TenantAdminUseCase {

    private final TenantRepository tenantRepository;
    private final UsuarioRepository usuarioRepository;
    private final TenantAdminApiMapper tenantAdminApiMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public PageResponse<TenantResponse> list(TenantFilter filter, int page, int size) {
        Pageable pageable = PageRequest.of(page, normalizeSize(size), Sort.by("nome").ascending());
        Page<TenantResponse> responsePage = tenantRepository.findAll(TenantSpecifications.withFilter(filter), pageable)
                .map(tenant -> tenantAdminApiMapper.toResponse(tenant, usuarioRepository.countByTenantId(tenant.getId())));
        return tenantAdminApiMapper.toPageResponse(responsePage);
    }

    @Transactional(readOnly = true)
    public TenantResponse getById(Long id) {
        Tenant tenant = findTenant(id);
        return tenantAdminApiMapper.toResponse(tenant, usuarioRepository.countByTenantId(tenant.getId()));
    }

    @Transactional
    public TenantResponse create(TenantRequest request) {
        validateSlug(request.slug(), null);
        validateTenantAdmin(request.admin(), null, null);
        Tenant tenant = tenantAdminApiMapper.toNewEntity(request);
        Tenant saved = tenantRepository.save(tenant);
        createTenantAdmin(saved, request.admin());
        auditService.log(AuditAction.TENANT_CRIADO,
                "Tenant '" + saved.getNome() + "' criado.", "Tenant", saved.getId(), "SUPER_ADMIN", null);
        return tenantAdminApiMapper.toResponse(saved, 1L);
    }

    @Transactional
    public TenantResponse update(Long id, TenantRequest request) {
        Tenant tenant = findTenant(id);
        validateSlug(request.slug(), id);
        validateTenantAdmin(request.admin(), tenant, null);
        tenantAdminApiMapper.updateEntity(tenant, request);
        Tenant saved = tenantRepository.save(tenant);

        if (request.admin() != null) {
            ensureTenantAdmin(saved, request.admin());
        }

        auditService.log(AuditAction.TENANT_ATUALIZADO,
                "Tenant '" + saved.getNome() + "' atualizado.", "Tenant", saved.getId(), "SUPER_ADMIN", null);
        return tenantAdminApiMapper.toResponse(saved, usuarioRepository.countByTenantId(saved.getId()));
    }

    @Transactional
    public void delete(Long id) {
        Tenant tenant = findTenant(id);

        if ("platform".equalsIgnoreCase(tenant.getSlug())) {
            throw new ConflictException("O tenant da plataforma não pode ser removido.");
        }

        if (usuarioRepository.countByTenantId(id) > 0) {
            throw new ConflictException("Não é possível remover tenant com usuários vinculados.");
        }

        tenantRepository.delete(tenant);
        auditService.log(AuditAction.TENANT_REMOVIDO,
                "Tenant '" + tenant.getNome() + "' removido.", "Tenant", tenant.getId(), "SUPER_ADMIN", null);
    }

    private Tenant findTenant(Long id) {
        return tenantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant não encontrado: " + id));
    }

    private void validateSlug(String slug, Long tenantId) {
        tenantRepository.findBySlugIgnoreCase(slug)
                .filter(existing -> !existing.getId().equals(tenantId))
                .ifPresent(existing -> {
                    throw new ConflictException("Já existe um tenant com o slug informado.");
                });
    }

    private void validateTenantAdmin(TenantAdminRequest request, Tenant tenant, Long usuarioId) {
        if (request == null) {
            throw new ConflictException("É obrigatório informar o admin inicial do tenant.");
        }

        Long tenantId = tenant != null ? tenant.getId() : null;

        if (tenantId != null) {
            usuarioRepository.findByEmailIgnoreCaseAndTenantId(request.email(), tenantId)
                    .filter(existing -> !existing.getId().equals(usuarioId))
                    .ifPresent(existing -> {
                        throw new ConflictException("Já existe um admin com este email no tenant informado.");
                    });
        }
    }

    private void createTenantAdmin(Tenant tenant, TenantAdminRequest request) {
        Usuario admin = Usuario.builder()
                .nome(request.nome())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .ativo(true)
                .role(Role.ADMIN)
                .tenant(tenant)
                .build();

        usuarioRepository.save(admin);
    }

    private void ensureTenantAdmin(Tenant tenant, TenantAdminRequest request) {
        usuarioRepository.findByEmailIgnoreCaseAndTenantId(request.email(), tenant.getId())
                .ifPresentOrElse(existing -> {
                    existing.setNome(request.nome());
                    existing.setAtivo(true);
                    existing.setRole(Role.ADMIN);

                    if (request.password() != null && !request.password().isBlank()) {
                        existing.setPassword(passwordEncoder.encode(request.password()));
                    }

                    usuarioRepository.save(existing);
                }, () -> createTenantAdmin(tenant, request));
    }

    private int normalizeSize(int size) {
        if (size <= 0) {
            return 20;
        }

        return Math.min(size, 100);
    }
}
