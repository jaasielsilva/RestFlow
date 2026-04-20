package com.jaasielsilva.erpcorporativo.app.usecase.web.crm;

import com.jaasielsilva.erpcorporativo.app.dto.web.crm.InteracaoForm;
import com.jaasielsilva.erpcorporativo.app.dto.web.crm.InteracaoViewModel;
import com.jaasielsilva.erpcorporativo.app.exception.ResourceNotFoundException;
import com.jaasielsilva.erpcorporativo.app.exception.ValidationException;
import com.jaasielsilva.erpcorporativo.app.model.Cliente;
import com.jaasielsilva.erpcorporativo.app.model.Interacao;
import com.jaasielsilva.erpcorporativo.app.model.Tenant;
import com.jaasielsilva.erpcorporativo.app.model.Usuario;
import com.jaasielsilva.erpcorporativo.app.repository.crm.ClienteRepository;
import com.jaasielsilva.erpcorporativo.app.repository.crm.InteracaoRepository;
import com.jaasielsilva.erpcorporativo.app.repository.tenant.TenantRepository;
import com.jaasielsilva.erpcorporativo.app.repository.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class InteracaoUseCase {

    private final InteracaoRepository interacaoRepository;
    private final ClienteRepository clienteRepository;
    private final TenantRepository tenantRepository;
    private final UsuarioRepository usuarioRepository;
    private final ClienteUseCase clienteUseCase;

    @Transactional
    public InteracaoViewModel create(Long tenantId, Long clienteId, InteracaoForm form) {
        if (form.getDataInteracao().isAfter(LocalDateTime.now())) {
            throw new ValidationException("A data da interação não pode ser futura.");
        }

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant não encontrado."));
        Cliente cliente = clienteRepository.findById(clienteId)
                .filter(c -> c.getTenant().getId().equals(tenantId))
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado."));

        String numero = generateNumero(tenantId);
        Usuario responsavel = resolveResponsavel(form.getResponsavelId());

        Interacao interacao = Interacao.builder()
                .numero(numero)
                .cliente(cliente)
                .tipo(form.getTipo())
                .dataInteracao(form.getDataInteracao())
                .assunto(form.getAssunto())
                .descricao(form.getDescricao())
                .responsavel(responsavel)
                .tenant(tenant)
                .build();

        Interacao saved = interacaoRepository.save(interacao);

        // Atualiza ultimaInteracaoEm do cliente
        if (cliente.getUltimaInteracaoEm() == null ||
                form.getDataInteracao().isAfter(cliente.getUltimaInteracaoEm())) {
            cliente.setUltimaInteracaoEm(form.getDataInteracao());
            clienteRepository.save(cliente);
        }

        return clienteUseCase.toInteracaoViewModel(saved);
    }

    @Transactional
    public InteracaoViewModel update(Long tenantId, Long id, InteracaoForm form) {
        if (form.getDataInteracao().isAfter(LocalDateTime.now())) {
            throw new ValidationException("A data da interação não pode ser futura.");
        }

        Interacao interacao = findOwned(tenantId, id);
        interacao.setTipo(form.getTipo());
        interacao.setDataInteracao(form.getDataInteracao());
        interacao.setAssunto(form.getAssunto());
        interacao.setDescricao(form.getDescricao());
        interacao.setResponsavel(resolveResponsavel(form.getResponsavelId()));

        return clienteUseCase.toInteracaoViewModel(interacaoRepository.save(interacao));
    }

    @Transactional
    public void delete(Long tenantId, Long id) {
        Interacao interacao = findOwned(tenantId, id);
        interacaoRepository.delete(interacao);
    }

    // -------------------------------------------------------------------------

    private Interacao findOwned(Long tenantId, Long id) {
        return interacaoRepository.findById(id)
                .filter(i -> i.getTenant().getId().equals(tenantId))
                .orElseThrow(() -> new ResourceNotFoundException("Interação não encontrada."));
    }

    private String generateNumero(Long tenantId) {
        int next = interacaoRepository.findMaxSequenceByTenantId(tenantId) + 1;
        return String.format("INT-%04d", next);
    }

    private Usuario resolveResponsavel(Long responsavelId) {
        if (responsavelId == null) return null;
        return usuarioRepository.findById(responsavelId).orElse(null);
    }
}
