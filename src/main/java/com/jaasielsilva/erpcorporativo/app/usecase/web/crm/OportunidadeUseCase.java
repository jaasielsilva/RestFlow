package com.jaasielsilva.erpcorporativo.app.usecase.web.crm;

import com.jaasielsilva.erpcorporativo.app.dto.web.crm.ClienteResumoViewModel;
import com.jaasielsilva.erpcorporativo.app.dto.web.crm.OportunidadeForm;
import com.jaasielsilva.erpcorporativo.app.dto.web.crm.OportunidadeFunilViewModel;
import com.jaasielsilva.erpcorporativo.app.dto.web.crm.OportunidadeViewModel;
import com.jaasielsilva.erpcorporativo.app.exception.ResourceNotFoundException;
import com.jaasielsilva.erpcorporativo.app.exception.ValidationException;
import com.jaasielsilva.erpcorporativo.app.model.*;
import com.jaasielsilva.erpcorporativo.app.repository.crm.ClienteRepository;
import com.jaasielsilva.erpcorporativo.app.repository.crm.ClienteSpecifications;
import com.jaasielsilva.erpcorporativo.app.repository.crm.OportunidadeRepository;
import com.jaasielsilva.erpcorporativo.app.repository.tenant.TenantRepository;
import com.jaasielsilva.erpcorporativo.app.repository.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OportunidadeUseCase {

    private static final List<StatusOportunidade> FECHADAS = List.of(
            StatusOportunidade.FECHADO_GANHO, StatusOportunidade.FECHADO_PERDIDO);

    private final OportunidadeRepository oportunidadeRepository;
    private final ClienteRepository clienteRepository;
    private final TenantRepository tenantRepository;
    private final UsuarioRepository usuarioRepository;
    private final ClienteUseCase clienteUseCase;

    @Transactional(readOnly = true)
    public OportunidadeFunilViewModel funil(Long tenantId) {
        List<Oportunidade> todas = oportunidadeRepository.findAll(
                (root, q, cb) -> cb.equal(root.get("tenant").get("id"), tenantId));

        Map<String, List<OportunidadeViewModel>> porStatus = new LinkedHashMap<>();
        for (StatusOportunidade s : StatusOportunidade.values()) {
            porStatus.put(s.name(), todas.stream()
                    .filter(o -> o.getStatus() == s)
                    .map(clienteUseCase::toOportunidadeViewModel)
                    .collect(Collectors.toList()));
        }

        long totalAbertas = oportunidadeRepository.countByTenantIdAndStatusNotIn(tenantId, FECHADAS);
        BigDecimal valorTotal = oportunidadeRepository.sumValorEstimadoByTenantIdAndStatusNotIn(tenantId, FECHADAS);

        LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);
        LocalDate fimMes = inicioMes.plusMonths(1).minusDays(1);
        long fechadasGanhasNoMes = oportunidadeRepository
                .countByTenantIdAndStatusAndDataFechamentoRealBetween(tenantId, StatusOportunidade.FECHADO_GANHO, inicioMes, fimMes);
        long totalFechadasNoMes = oportunidadeRepository
                .countByTenantIdAndStatusAndDataFechamentoRealBetween(tenantId, StatusOportunidade.FECHADO_PERDIDO, inicioMes, fimMes)
                + fechadasGanhasNoMes;

        List<ClienteResumoViewModel> clientes = clienteRepository.findAll(
                        ClienteSpecifications.byTenant(tenantId),
                        org.springframework.data.domain.PageRequest.of(0, 200, org.springframework.data.domain.Sort.by("nome")))
                .getContent().stream()
                .map(c -> new ClienteResumoViewModel(c.getId(), c.getNumero(), c.getTipo(), c.getNome(),
                        null, c.getEmail(), c.getTelefonePrincipal(), c.getStatus(), c.getUltimaInteracaoEm(), 0))
                .toList();

        return new OportunidadeFunilViewModel(porStatus, totalAbertas,
                valorTotal != null ? valorTotal : BigDecimal.ZERO,
                fechadasGanhasNoMes, totalFechadasNoMes, clientes);
    }

    @Transactional(readOnly = true)
    public OportunidadeViewModel getById(Long tenantId, Long id) {
        return clienteUseCase.toOportunidadeViewModel(findOwned(tenantId, id));
    }

    @Transactional
    public OportunidadeViewModel create(Long tenantId, OportunidadeForm form) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant não encontrado."));
        Cliente cliente = clienteRepository.findById(form.getClienteId())
                .filter(c -> c.getTenant().getId().equals(tenantId))
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado."));

        if (form.getStatus() == StatusOportunidade.FECHADO_PERDIDO && !StringUtils.hasText(form.getMotivoPerda())) {
            throw new ValidationException("Motivo da perda é obrigatório.");
        }

        String numero = generateNumero(tenantId);
        Oportunidade oportunidade = Oportunidade.builder()
                .numero(numero)
                .cliente(cliente)
                .titulo(form.getTitulo())
                .status(form.getStatus())
                .valorEstimado(form.getValorEstimado())
                .dataPrevistaFechamento(form.getDataPrevistaFechamento())
                .descricao(form.getDescricao())
                .motivoPerda(form.getMotivoPerda())
                .responsavel(resolveResponsavel(form.getResponsavelId()))
                .dataFechamentoReal(form.getStatus() == StatusOportunidade.FECHADO_GANHO ? LocalDate.now() : null)
                .tenant(tenant)
                .build();

        return clienteUseCase.toOportunidadeViewModel(oportunidadeRepository.save(oportunidade));
    }

    @Transactional
    public OportunidadeViewModel update(Long tenantId, Long id, OportunidadeForm form) {
        Oportunidade oportunidade = findOwned(tenantId, id);

        if (form.getStatus() == StatusOportunidade.FECHADO_PERDIDO && !StringUtils.hasText(form.getMotivoPerda())) {
            throw new ValidationException("Motivo da perda é obrigatório.");
        }

        oportunidade.setTitulo(form.getTitulo());
        oportunidade.setStatus(form.getStatus());
        oportunidade.setValorEstimado(form.getValorEstimado());
        oportunidade.setDataPrevistaFechamento(form.getDataPrevistaFechamento());
        oportunidade.setDescricao(form.getDescricao());
        oportunidade.setMotivoPerda(form.getMotivoPerda());
        oportunidade.setResponsavel(resolveResponsavel(form.getResponsavelId()));

        if (form.getStatus() == StatusOportunidade.FECHADO_GANHO && oportunidade.getDataFechamentoReal() == null) {
            oportunidade.setDataFechamentoReal(LocalDate.now());
        }

        return clienteUseCase.toOportunidadeViewModel(oportunidadeRepository.save(oportunidade));
    }

    @Transactional
    public void delete(Long tenantId, Long id) {
        Oportunidade oportunidade = findOwned(tenantId, id);
        oportunidadeRepository.delete(oportunidade);
    }

    // -------------------------------------------------------------------------

    private Oportunidade findOwned(Long tenantId, Long id) {
        return oportunidadeRepository.findById(id)
                .filter(o -> o.getTenant().getId().equals(tenantId))
                .orElseThrow(() -> new ResourceNotFoundException("Oportunidade não encontrada."));
    }

    private String generateNumero(Long tenantId) {
        int next = oportunidadeRepository.findMaxSequenceByTenantId(tenantId) + 1;
        return String.format("OPO-%04d", next);
    }

    private Usuario resolveResponsavel(Long responsavelId) {
        if (responsavelId == null) return null;
        return usuarioRepository.findById(responsavelId).orElse(null);
    }
}
