package com.jaasielsilva.erpcorporativo.app.usecase.web.crm;

import com.jaasielsilva.erpcorporativo.app.dto.web.crm.*;
import com.jaasielsilva.erpcorporativo.app.exception.ConflictException;
import com.jaasielsilva.erpcorporativo.app.exception.ResourceNotFoundException;
import com.jaasielsilva.erpcorporativo.app.exception.ValidationException;
import com.jaasielsilva.erpcorporativo.app.model.*;
import com.jaasielsilva.erpcorporativo.app.repository.crm.ClienteRepository;
import com.jaasielsilva.erpcorporativo.app.repository.crm.ClienteSpecifications;
import com.jaasielsilva.erpcorporativo.app.repository.crm.InteracaoRepository;
import com.jaasielsilva.erpcorporativo.app.repository.crm.OportunidadeRepository;
import com.jaasielsilva.erpcorporativo.app.repository.tenant.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ClienteUseCase {

    private final ClienteRepository clienteRepository;
    private final InteracaoRepository interacaoRepository;
    private final OportunidadeRepository oportunidadeRepository;
    private final TenantRepository tenantRepository;

    @Transactional(readOnly = true)
    public ClienteListViewModel list(Long tenantId, String nome, TipoCliente tipo,
                                     StatusCliente status, String documento, int page, int size) {
        Specification<Cliente> spec = Specification.allOf(
                ClienteSpecifications.byTenant(tenantId),
                ClienteSpecifications.byNome(nome),
                ClienteSpecifications.byTipo(tipo),
                ClienteSpecifications.byStatus(status),
                ClienteSpecifications.byDocumento(documento)
        );

        Page<Cliente> pageResult = clienteRepository.findAll(spec,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));

        List<ClienteResumoViewModel> items = pageResult.getContent().stream()
                .map(c -> toResumoViewModel(c, tenantId))
                .toList();

        return new ClienteListViewModel(
                items,
                pageResult.getNumber(),
                pageResult.getTotalPages(),
                pageResult.getTotalElements(),
                clienteRepository.countByTenantIdAndStatus(tenantId, StatusCliente.ATIVO),
                clienteRepository.countByTenantIdAndStatus(tenantId, StatusCliente.INATIVO),
                clienteRepository.countByTenantIdAndStatus(tenantId, StatusCliente.PROSPECTO),
                clienteRepository.countByTenantIdAndStatus(tenantId, StatusCliente.BLOQUEADO)
        );
    }

    @Transactional(readOnly = true)
    public ClienteViewModel getById(Long tenantId, Long id) {
        Cliente cliente = findOwned(tenantId, id);
        List<InteracaoViewModel> interacoes = interacaoRepository
                .findByClienteIdOrderByDataInteracaoDesc(id)
                .stream().map(this::toInteracaoViewModel).toList();
        List<OportunidadeViewModel> oportunidades = oportunidadeRepository
                .findByClienteIdOrderByCreatedAtDesc(id)
                .stream().map(this::toOportunidadeViewModel).toList();
        long oportunidadesAbertas = oportunidadeRepository.countByClienteIdAndStatusNotIn(
                id, List.of(StatusOportunidade.FECHADO_GANHO, StatusOportunidade.FECHADO_PERDIDO));
        return toViewModel(cliente, interacoes, oportunidades, oportunidadesAbertas);
    }

    @Transactional
    public ClienteViewModel create(Long tenantId, ClienteForm form) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant não encontrado."));

        if (StringUtils.hasText(form.getDocumento())) {
            clienteRepository.findByTenantIdAndDocumento(tenantId, form.getDocumento())
                    .ifPresent(c -> { throw new ConflictException("CPF/CNPJ já cadastrado para outro cliente."); });
        }

        if (form.getStatus() == StatusCliente.BLOQUEADO && !StringUtils.hasText(form.getMotivoBloqueio())) {
            throw new ValidationException("Motivo do bloqueio é obrigatório ao bloquear um cliente.");
        }

        String numero = generateNumeroCliente(tenantId);
        Cliente cliente = buildCliente(form, tenant, numero);
        return toViewModel(clienteRepository.save(cliente), List.of(), List.of(), 0);
    }

    @Transactional
    public ClienteViewModel update(Long tenantId, Long id, ClienteForm form) {
        Cliente cliente = findOwned(tenantId, id);

        if (StringUtils.hasText(form.getDocumento()) &&
                clienteRepository.existsByTenantIdAndDocumentoAndIdNot(tenantId, form.getDocumento(), id)) {
            throw new ConflictException("CPF/CNPJ já cadastrado para outro cliente.");
        }

        if (form.getStatus() == StatusCliente.BLOQUEADO && !StringUtils.hasText(form.getMotivoBloqueio())) {
            throw new ValidationException("Motivo do bloqueio é obrigatório ao bloquear um cliente.");
        }

        applyForm(cliente, form);
        return toViewModel(clienteRepository.save(cliente), List.of(), List.of(), 0);
    }

    @Transactional
    public void updateStatus(Long tenantId, Long id, StatusCliente novoStatus, String motivoBloqueio) {
        Cliente cliente = findOwned(tenantId, id);
        if (novoStatus == StatusCliente.BLOQUEADO && !StringUtils.hasText(motivoBloqueio)) {
            throw new ValidationException("Motivo do bloqueio é obrigatório.");
        }
        cliente.setStatus(novoStatus);
        if (novoStatus == StatusCliente.BLOQUEADO) {
            cliente.setMotivoBloqueio(motivoBloqueio);
        } else {
            cliente.setMotivoBloqueio(null);
        }
        clienteRepository.save(cliente);
    }

    @Transactional
    public void delete(Long tenantId, Long id) {
        Cliente cliente = findOwned(tenantId, id);
        long oportunidadesAtivas = oportunidadeRepository.countByClienteIdAndStatusNotIn(
                id, List.of(StatusOportunidade.FECHADO_GANHO, StatusOportunidade.FECHADO_PERDIDO));
        if (oportunidadesAtivas > 0) {
            throw new ValidationException("Não é possível excluir o cliente pois possui " + oportunidadesAtivas + " oportunidade(s) ativa(s).");
        }
        clienteRepository.delete(cliente);
    }

    // -------------------------------------------------------------------------

    private Cliente findOwned(Long tenantId, Long id) {
        return clienteRepository.findById(id)
                .filter(c -> c.getTenant().getId().equals(tenantId))
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado."));
    }

    private String generateNumeroCliente(Long tenantId) {
        int next = clienteRepository.findMaxSequenceByTenantId(tenantId) + 1;
        return String.format("CLI-%04d", next);
    }

    private Cliente buildCliente(ClienteForm form, Tenant tenant, String numero) {
        return Cliente.builder()
                .numero(numero)
                .tipo(form.getTipo())
                .nome(form.getNome())
                .documento(StringUtils.hasText(form.getDocumento()) ? form.getDocumento() : null)
                .email(form.getEmail())
                .telefonePrincipal(form.getTelefonePrincipal())
                .telefoneSecundario(form.getTelefoneSecundario())
                .logradouro(form.getLogradouro())
                .numero_endereco(form.getNumeroEndereco())
                .complemento(form.getComplemento())
                .bairro(form.getBairro())
                .cidade(form.getCidade())
                .estado(form.getEstado())
                .cep(form.getCep())
                .status(form.getStatus())
                .observacoes(form.getObservacoes())
                .dataNascimento(form.getDataNascimento())
                .genero(form.getGenero())
                .nomeFantasia(form.getNomeFantasia())
                .inscricaoEstadual(form.getInscricaoEstadual())
                .contatoPrincipal(form.getContatoPrincipal())
                .motivoBloqueio(form.getMotivoBloqueio())
                .tenant(tenant)
                .build();
    }

    private void applyForm(Cliente cliente, ClienteForm form) {
        cliente.setTipo(form.getTipo());
        cliente.setNome(form.getNome());
        cliente.setDocumento(StringUtils.hasText(form.getDocumento()) ? form.getDocumento() : null);
        cliente.setEmail(form.getEmail());
        cliente.setTelefonePrincipal(form.getTelefonePrincipal());
        cliente.setTelefoneSecundario(form.getTelefoneSecundario());
        cliente.setLogradouro(form.getLogradouro());
        cliente.setNumero_endereco(form.getNumeroEndereco());
        cliente.setComplemento(form.getComplemento());
        cliente.setBairro(form.getBairro());
        cliente.setCidade(form.getCidade());
        cliente.setEstado(form.getEstado());
        cliente.setCep(form.getCep());
        cliente.setStatus(form.getStatus());
        cliente.setObservacoes(form.getObservacoes());
        cliente.setDataNascimento(form.getDataNascimento());
        cliente.setGenero(form.getGenero());
        cliente.setNomeFantasia(form.getNomeFantasia());
        cliente.setInscricaoEstadual(form.getInscricaoEstadual());
        cliente.setContatoPrincipal(form.getContatoPrincipal());
        cliente.setMotivoBloqueio(form.getMotivoBloqueio());
    }

    private ClienteResumoViewModel toResumoViewModel(Cliente c, Long tenantId) {
        long oportunidadesAbertas = oportunidadeRepository.countByClienteIdAndStatusNotIn(
                c.getId(), List.of(StatusOportunidade.FECHADO_GANHO, StatusOportunidade.FECHADO_PERDIDO));
        return new ClienteResumoViewModel(
                c.getId(), c.getNumero(), c.getTipo(), c.getNome(),
                mascarar(c.getDocumento()), c.getEmail(), c.getTelefonePrincipal(),
                c.getStatus(), c.getUltimaInteracaoEm(), oportunidadesAbertas
        );
    }

    private ClienteViewModel toViewModel(Cliente c, List<InteracaoViewModel> interacoes,
                                          List<OportunidadeViewModel> oportunidades, long oportunidadesAbertas) {
        return new ClienteViewModel(
                c.getId(), c.getNumero(), c.getTipo(), c.getNome(), c.getDocumento(),
                c.getEmail(), c.getTelefonePrincipal(), c.getTelefoneSecundario(),
                c.getLogradouro(), c.getNumero_endereco(), c.getComplemento(),
                c.getBairro(), c.getCidade(), c.getEstado(), c.getCep(),
                c.getStatus(), c.getObservacoes(), c.getDataNascimento(), c.getGenero(),
                c.getNomeFantasia(), c.getInscricaoEstadual(), c.getContatoPrincipal(),
                c.getMotivoBloqueio(), c.getUltimaInteracaoEm(), oportunidadesAbertas,
                interacoes, oportunidades, c.getCreatedAt(), c.getUpdatedAt()
        );
    }

    InteracaoViewModel toInteracaoViewModel(com.jaasielsilva.erpcorporativo.app.model.Interacao i) {
        return new InteracaoViewModel(
                i.getId(), i.getNumero(), i.getCliente().getId(), i.getCliente().getNome(),
                i.getTipo(), i.getDataInteracao(), i.getAssunto(), i.getDescricao(),
                i.getResponsavel() != null ? i.getResponsavel().getNome() : null,
                i.getResponsavel() != null ? i.getResponsavel().getId() : null,
                i.getCreatedAt()
        );
    }

    OportunidadeViewModel toOportunidadeViewModel(com.jaasielsilva.erpcorporativo.app.model.Oportunidade o) {
        return new OportunidadeViewModel(
                o.getId(), o.getNumero(), o.getCliente().getId(), o.getCliente().getNome(),
                o.getTitulo(), o.getStatus(), o.getValorEstimado(), o.getDataPrevistaFechamento(),
                o.getDataFechamentoReal(), o.getMotivoPerda(), o.getDescricao(),
                o.getResponsavel() != null ? o.getResponsavel().getNome() : null,
                o.getResponsavel() != null ? o.getResponsavel().getId() : null,
                o.getCreatedAt(), o.getUpdatedAt()
        );
    }

    private String mascarar(String doc) {
        if (doc == null || doc.isBlank()) return null;
        if (doc.length() <= 4) return "****";
        return doc.substring(0, 3) + "***" + doc.substring(doc.length() - 2);
    }
}
