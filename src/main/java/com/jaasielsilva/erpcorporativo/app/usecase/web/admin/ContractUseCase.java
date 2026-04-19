package com.jaasielsilva.erpcorporativo.app.usecase.web.admin;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.jaasielsilva.erpcorporativo.app.dto.web.admin.contract.ContractForm;
import com.jaasielsilva.erpcorporativo.app.dto.web.admin.contract.ContractListViewModel;
import com.jaasielsilva.erpcorporativo.app.dto.web.admin.contract.ContractViewModel;
import com.jaasielsilva.erpcorporativo.app.dto.web.admin.contract.PaymentRecordForm;
import com.jaasielsilva.erpcorporativo.app.dto.web.admin.contract.PaymentRecordViewModel;
import com.jaasielsilva.erpcorporativo.app.exception.ConflictException;
import com.jaasielsilva.erpcorporativo.app.exception.ResourceNotFoundException;
import com.jaasielsilva.erpcorporativo.app.model.AuditAction;
import com.jaasielsilva.erpcorporativo.app.model.Contract;
import com.jaasielsilva.erpcorporativo.app.model.ContractStatus;
import com.jaasielsilva.erpcorporativo.app.model.PaymentRecord;
import com.jaasielsilva.erpcorporativo.app.model.PaymentStatus;
import com.jaasielsilva.erpcorporativo.app.model.SubscriptionPlan;
import com.jaasielsilva.erpcorporativo.app.model.Tenant;
import com.jaasielsilva.erpcorporativo.app.repository.contract.ContractRepository;
import com.jaasielsilva.erpcorporativo.app.repository.contract.ContractSpecifications;
import com.jaasielsilva.erpcorporativo.app.repository.contract.PaymentRecordRepository;
import com.jaasielsilva.erpcorporativo.app.repository.plan.SubscriptionPlanRepository;
import com.jaasielsilva.erpcorporativo.app.repository.tenant.TenantRepository;
import com.jaasielsilva.erpcorporativo.app.service.shared.AuditService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ContractUseCase {

    private final ContractRepository contractRepository;
    private final PaymentRecordRepository paymentRecordRepository;
    private final TenantRepository tenantRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final AuditService auditService;

    // -------------------------------------------------------------------------
    // Contract CRUD
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public ContractListViewModel list(String tenantNome, ContractStatus status,
                                      String planoCodigo, int page, int size) {
        Specification<Contract> spec = Specification.allOf(
                ContractSpecifications.byTenantNome(tenantNome),
                ContractSpecifications.byStatus(status),
                ContractSpecifications.byPlanoCodigo(planoCodigo)
        );

        Page<Contract> pageResult = contractRepository.findAll(spec,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));

        List<ContractViewModel> items = pageResult.getContent().stream()
                .map(c -> toViewModel(c, List.of()))
                .toList();

        long totalAtivos = contractRepository.countByStatus(ContractStatus.ATIVO);
        long totalVencidos = contractRepository.countByStatusAndDataTerminoBefore(ContractStatus.ATIVO, LocalDate.now());
        long totalAtrasados = paymentRecordRepository.countByStatus(PaymentStatus.ATRASADO);
        var mrrTotal = contractRepository.sumValorMensalByStatus(ContractStatus.ATIVO);

        return new ContractListViewModel(
                items,
                pageResult.getNumber(),
                pageResult.getTotalPages(),
                pageResult.getTotalElements(),
                totalAtivos,
                totalVencidos,
                totalAtrasados,
                mrrTotal
        );
    }

    @Transactional(readOnly = true)
    public ContractViewModel getById(Long id) {
        Contract contract = findContract(id);
        List<PaymentRecordViewModel> pagamentos = paymentRecordRepository
                .findByContractIdOrderByMesReferenciaDesc(id)
                .stream()
                .map(this::toPaymentViewModel)
                .toList();
        return toViewModel(contract, pagamentos);
    }

    @Transactional
    public ContractViewModel create(ContractForm form, String executadoPor) {
        Tenant tenant = tenantRepository.findById(form.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Tenant não encontrado."));

        if (form.getStatus() == ContractStatus.ATIVO
                && contractRepository.existsByTenantIdAndStatus(tenant.getId(), ContractStatus.ATIVO)) {
            throw new ConflictException("Este tenant já possui um contrato ATIVO. Encerre-o antes de criar um novo.");
        }

        SubscriptionPlan plan = subscriptionPlanRepository.findById(form.getSubscriptionPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Plano não encontrado."));

        Contract contract = Contract.builder()
                .tenant(tenant)
                .subscriptionPlan(plan)
                .valorMensal(form.getValorMensal())
                .dataInicio(form.getDataInicio())
                .dataTermino(form.getDataTermino())
                .status(form.getStatus())
                .observacoes(form.getObservacoes())
                .diaVencimento(form.getDiaVencimento() > 0 ? form.getDiaVencimento() : 1)
                .build();

        Contract saved = contractRepository.save(contract);

        // Atualiza o plano do tenant automaticamente
        tenant.setSubscriptionPlan(plan);
        tenantRepository.save(tenant);

        auditService.log(AuditAction.CONTRACT_CRIADO,
                "Contrato criado para tenant '" + tenant.getNome() + "' — plano: " + plan.getNome(),
                "Contract", saved.getId(), executadoPor, tenant);

        return toViewModel(saved, List.of());
    }

    @Transactional
    public ContractViewModel update(Long id, ContractForm form, String executadoPor) {
        Contract contract = findContract(id);

        SubscriptionPlan plan = subscriptionPlanRepository.findById(form.getSubscriptionPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Plano não encontrado."));

        contract.setSubscriptionPlan(plan);
        contract.setValorMensal(form.getValorMensal());
        contract.setDataInicio(form.getDataInicio());
        contract.setDataTermino(form.getDataTermino());
        contract.setStatus(form.getStatus());
        contract.setObservacoes(form.getObservacoes());
        contract.setDiaVencimento(form.getDiaVencimento() > 0 ? form.getDiaVencimento() : 1);

        Contract saved = contractRepository.save(contract);

        auditService.log(AuditAction.CONTRACT_ATUALIZADO,
                "Contrato #" + id + " atualizado para tenant '" + contract.getTenant().getNome() + "'",
                "Contract", id, executadoPor, contract.getTenant());

        return toViewModel(saved, List.of());
    }

    @Transactional
    public void updateStatus(Long id, ContractStatus novoStatus, String executadoPor) {
        Contract contract = findContract(id);
        contract.setStatus(novoStatus);
        contractRepository.save(contract);

        AuditAction action = switch (novoStatus) {
            case ENCERRADO -> AuditAction.CONTRACT_ENCERRADO;
            case SUSPENSO -> AuditAction.CONTRACT_SUSPENSO;
            default -> AuditAction.CONTRACT_ATUALIZADO;
        };

        auditService.log(action,
                "Contrato #" + id + " alterado para " + novoStatus + " — tenant: " + contract.getTenant().getNome(),
                "Contract", id, executadoPor, contract.getTenant());
    }

    @Transactional
    public void delete(Long id, String executadoPor) {
        Contract contract = findContract(id);
        String tenantNome = contract.getTenant().getNome();
        contractRepository.delete(contract);
        auditService.log(AuditAction.CONTRACT_REMOVIDO,
                "Contrato #" + id + " removido — tenant: " + tenantNome,
                "Contract", id, executadoPor, null);
    }

    // -------------------------------------------------------------------------
    // Payment records
    // -------------------------------------------------------------------------

    @Transactional
    public PaymentRecordViewModel addPayment(Long contractId, PaymentRecordForm form, String executadoPor) {
        Contract contract = findContract(contractId);

        PaymentRecord payment = PaymentRecord.builder()
                .contract(contract)
                .mesReferencia(form.getMesReferencia())
                .valorPago(form.getValorPago())
                .dataPagamento(form.getDataPagamento())
                .status(form.getStatus())
                .observacoes(form.getObservacoes())
                .build();

        PaymentRecord saved = paymentRecordRepository.save(payment);

        auditService.log(AuditAction.PAGAMENTO_REGISTRADO,
                "Pagamento registrado para contrato #" + contractId + " — mês: " + form.getMesReferencia(),
                "PaymentRecord", saved.getId(), executadoPor, contract.getTenant());

        return toPaymentViewModel(saved);
    }

    @Transactional
    public PaymentRecordViewModel updatePayment(Long contractId, Long paymentId,
                                                 PaymentRecordForm form, String executadoPor) {
        PaymentRecord payment = paymentRecordRepository.findById(paymentId)
                .filter(p -> p.getContract().getId().equals(contractId))
                .orElseThrow(() -> new ResourceNotFoundException("Pagamento não encontrado."));

        payment.setStatus(form.getStatus());
        payment.setObservacoes(form.getObservacoes());
        payment.setValorPago(form.getValorPago());
        payment.setDataPagamento(form.getDataPagamento());

        PaymentRecord saved = paymentRecordRepository.save(payment);

        auditService.log(AuditAction.PAGAMENTO_ATUALIZADO,
                "Pagamento #" + paymentId + " atualizado — contrato #" + contractId,
                "PaymentRecord", paymentId, executadoPor, payment.getContract().getTenant());

        return toPaymentViewModel(saved);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Contract findContract(Long id) {
        return contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato não encontrado: " + id));
    }

    private ContractViewModel toViewModel(Contract c, List<PaymentRecordViewModel> pagamentos) {
        LocalDate hoje = LocalDate.now();
        boolean isVencido = c.getDataTermino() != null
                && c.getDataTermino().isBefore(hoje)
                && c.getStatus() == ContractStatus.ATIVO;
        boolean isVencendoEm30Dias = c.getDataTermino() != null
                && !c.getDataTermino().isBefore(hoje)
                && !c.getDataTermino().isAfter(hoje.plusDays(30))
                && c.getStatus() == ContractStatus.ATIVO;

        PaymentStatus ultimoPagamento = paymentRecordRepository
                .findFirstByContractIdOrderByMesReferenciaDesc(c.getId())
                .map(PaymentRecord::getStatus)
                .orElse(null);

        return new ContractViewModel(
                c.getId(),
                c.getTenant().getId(),
                c.getTenant().getNome(),
                c.getSubscriptionPlan() != null ? c.getSubscriptionPlan().getId() : null,
                c.getSubscriptionPlan() != null ? c.getSubscriptionPlan().getNome() : null,
                c.getValorMensal(),
                c.getDataInicio(),
                c.getDataTermino(),
                c.getStatus(),
                c.getObservacoes(),
                c.getDiaVencimento(),
                isVencido,
                isVencendoEm30Dias,
                ultimoPagamento,
                pagamentos,
                c.getCreatedAt(),
                c.getUpdatedAt()
        );
    }

    private PaymentRecordViewModel toPaymentViewModel(PaymentRecord p) {
        return new PaymentRecordViewModel(
                p.getId(),
                p.getMesReferencia(),
                p.getValorPago(),
                p.getDataPagamento(),
                p.getStatus(),
                p.getStatus() == PaymentStatus.ATRASADO,
                p.getObservacoes(),
                p.getCreatedAt()
        );
    }
}
