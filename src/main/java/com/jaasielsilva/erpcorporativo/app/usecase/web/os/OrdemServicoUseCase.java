package com.jaasielsilva.erpcorporativo.app.usecase.web.os;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.jaasielsilva.erpcorporativo.app.dto.web.os.OrdemServicoForm;
import com.jaasielsilva.erpcorporativo.app.dto.web.os.OrdemServicoListViewModel;
import com.jaasielsilva.erpcorporativo.app.dto.web.os.OrdemServicoViewModel;
import com.jaasielsilva.erpcorporativo.app.exception.ResourceNotFoundException;
import com.jaasielsilva.erpcorporativo.app.model.OrdemServico;
import com.jaasielsilva.erpcorporativo.app.model.OrdemServicoStatus;
import com.jaasielsilva.erpcorporativo.app.model.Tenant;
import com.jaasielsilva.erpcorporativo.app.model.Usuario;
import com.jaasielsilva.erpcorporativo.app.repository.os.OrdemServicoRepository;
import com.jaasielsilva.erpcorporativo.app.repository.os.OrdemServicoSpecifications;
import com.jaasielsilva.erpcorporativo.app.repository.tenant.TenantRepository;
import com.jaasielsilva.erpcorporativo.app.repository.usuario.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrdemServicoUseCase {

    private final OrdemServicoRepository osRepository;
    private final TenantRepository tenantRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public OrdemServicoListViewModel list(Long tenantId, String titulo, String cliente,
                                          OrdemServicoStatus status, int page, int size) {
        Specification<OrdemServico> spec = Specification
                .allOf(
                        OrdemServicoSpecifications.byTenant(tenantId),
                        OrdemServicoSpecifications.byTitulo(titulo),
                        OrdemServicoSpecifications.byCliente(cliente),
                        OrdemServicoSpecifications.byStatus(status)
                );

        Page<OrdemServico> pageResult = osRepository.findAll(spec,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));

        List<OrdemServicoViewModel> items = pageResult.getContent().stream()
                .map(this::toViewModel)
                .toList();

        return new OrdemServicoListViewModel(
                items,
                pageResult.getNumber(),
                pageResult.getTotalPages(),
                pageResult.getTotalElements(),
                osRepository.countByTenantIdAndStatus(tenantId, OrdemServicoStatus.ABERTA),
                osRepository.countByTenantIdAndStatus(tenantId, OrdemServicoStatus.EM_ANDAMENTO),
                osRepository.countByTenantIdAndStatus(tenantId, OrdemServicoStatus.CONCLUIDA),
                osRepository.countByTenantIdAndStatus(tenantId, OrdemServicoStatus.CANCELADA)
        );
    }

    @Transactional(readOnly = true)
    public OrdemServicoViewModel getById(Long tenantId, Long id) {
        OrdemServico os = findOwned(tenantId, id);
        return toViewModel(os);
    }

    @Transactional
    public OrdemServicoViewModel create(Long tenantId, OrdemServicoForm form) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant não encontrado."));

        String numero = generateNumero(tenantId);
        Usuario responsavel = resolveResponsavel(form.getResponsavelId());

        OrdemServico os = OrdemServico.builder()
                .numero(numero)
                .titulo(form.getTitulo())
                .descricao(form.getDescricao())
                .clienteNome(form.getClienteNome())
                .clienteEmail(form.getClienteEmail())
                .clienteTelefone(form.getClienteTelefone())
                .status(form.getStatus())
                .valor(form.getValor())
                .dataPrevista(form.getDataPrevista())
                .responsavel(responsavel)
                .tenant(tenant)
                .build();

        return toViewModel(osRepository.save(os));
    }

    @Transactional
    public OrdemServicoViewModel update(Long tenantId, Long id, OrdemServicoForm form) {
        OrdemServico os = findOwned(tenantId, id);

        os.setTitulo(form.getTitulo());
        os.setDescricao(form.getDescricao());
        os.setClienteNome(form.getClienteNome());
        os.setClienteEmail(form.getClienteEmail());
        os.setClienteTelefone(form.getClienteTelefone());
        os.setStatus(form.getStatus());
        os.setValor(form.getValor());
        os.setDataPrevista(form.getDataPrevista());
        os.setResponsavel(resolveResponsavel(form.getResponsavelId()));

        return toViewModel(osRepository.save(os));
    }

    @Transactional
    public void updateStatus(Long tenantId, Long id, OrdemServicoStatus novoStatus) {
        OrdemServico os = findOwned(tenantId, id);
        os.setStatus(novoStatus);
        osRepository.save(os);
    }

    @Transactional
    public void delete(Long tenantId, Long id) {
        OrdemServico os = findOwned(tenantId, id);
        osRepository.delete(os);
    }

    // -------------------------------------------------------------------------

    private OrdemServico findOwned(Long tenantId, Long id) {
        return osRepository.findById(id)
                .filter(os -> os.getTenant().getId().equals(tenantId))
                .orElseThrow(() -> new ResourceNotFoundException("Ordem de serviço não encontrada."));
    }

    private String generateNumero(Long tenantId) {
        int next = osRepository.findMaxSequenceByTenantId(tenantId) + 1;
        return String.format("OS-%04d", next);
    }

    private Usuario resolveResponsavel(Long responsavelId) {
        if (responsavelId == null) return null;
        return usuarioRepository.findById(responsavelId).orElse(null);
    }

    private OrdemServicoViewModel toViewModel(OrdemServico os) {
        return new OrdemServicoViewModel(
                os.getId(),
                os.getNumero(),
                os.getTitulo(),
                os.getDescricao(),
                os.getClienteNome(),
                os.getClienteEmail(),
                os.getClienteTelefone(),
                os.getStatus(),
                os.getValor(),
                os.getDataPrevista(),
                os.getResponsavel() != null ? os.getResponsavel().getNome() : null,
                os.getResponsavel() != null ? os.getResponsavel().getId() : null,
                os.getCreatedAt(),
                os.getUpdatedAt()
        );
    }
}
