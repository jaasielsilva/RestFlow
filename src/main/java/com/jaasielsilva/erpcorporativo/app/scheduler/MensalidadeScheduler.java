package com.jaasielsilva.erpcorporativo.app.scheduler;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.jaasielsilva.erpcorporativo.app.model.Contract;
import com.jaasielsilva.erpcorporativo.app.model.ContractStatus;
import com.jaasielsilva.erpcorporativo.app.model.PaymentRecord;
import com.jaasielsilva.erpcorporativo.app.model.PaymentStatus;
import com.jaasielsilva.erpcorporativo.app.repository.contract.ContractRepository;
import com.jaasielsilva.erpcorporativo.app.repository.contract.PaymentRecordRepository;

import lombok.RequiredArgsConstructor;

/**
 * Job que roda todo dia às 02:00 e gera automaticamente o registro de mensalidade
 * para cada contrato ATIVO cujo dia de vencimento é hoje.
 *
 * Regras:
 * - Só gera se o contrato está ATIVO
 * - Só gera se ainda não existe pagamento para o mês atual
 * - Usa o valor do contrato como valor esperado
 * - Status inicial: PENDENTE (o SUPER_ADMIN confirma quando receber)
 * - Idempotente: pode rodar múltiplas vezes no mesmo dia sem duplicar
 */
@Component
@RequiredArgsConstructor
public class MensalidadeScheduler {

    private static final Logger log = LoggerFactory.getLogger(MensalidadeScheduler.class);

    private final ContractRepository contractRepository;
    private final PaymentRecordRepository paymentRecordRepository;

    @Scheduled(cron = "0 0 2 * * ?") // todo dia às 02:00
    @Transactional
    public void gerarMensalidades() {
        LocalDate hoje = LocalDate.now();
        int diaHoje = hoje.getDayOfMonth();
        YearMonth mesAtual = YearMonth.now();

        log.info("[MensalidadeScheduler] Iniciando geração de mensalidades — dia {} / mês {}", diaHoje, mesAtual);

        List<Contract> contratosAtivos = contractRepository.findAll().stream()
                .filter(c -> c.getStatus() == ContractStatus.ATIVO)
                .filter(c -> c.getDiaVencimento() == diaHoje)
                .filter(c -> contratoVigenteHoje(c, hoje))
                .toList();

        int gerados = 0;
        int ignorados = 0;

        for (Contract contract : contratosAtivos) {
            boolean jaExiste = paymentRecordRepository
                    .findByContractIdOrderByMesReferenciaDesc(contract.getId())
                    .stream()
                    .anyMatch(p -> mesAtual.equals(p.getMesReferencia()));

            if (jaExiste) {
                log.debug("[MensalidadeScheduler] Contrato #{} — pagamento de {} já existe, ignorando.",
                        contract.getId(), mesAtual);
                ignorados++;
                continue;
            }

            PaymentRecord payment = PaymentRecord.builder()
                    .contract(contract)
                    .mesReferencia(mesAtual)
                    .valorPago(contract.getValorMensal())
                    .dataPagamento(null) // ainda não pago
                    .status(PaymentStatus.PENDENTE)
                    .observacoes("Gerado automaticamente pelo sistema em " + hoje)
                    .build();

            paymentRecordRepository.save(payment);
            gerados++;

            log.info("[MensalidadeScheduler] Mensalidade gerada — contrato #{} / tenant '{}' / mês {} / valor R$ {}",
                    contract.getId(),
                    contract.getTenant().getNome(),
                    mesAtual,
                    contract.getValorMensal());
        }

        log.info("[MensalidadeScheduler] Concluído — {} gerados, {} ignorados (já existiam).", gerados, ignorados);
    }

    /**
     * Verifica se o contrato está vigente hoje:
     * - dataInicio <= hoje
     * - dataTermino é null (sem prazo) ou dataTermino >= hoje
     */
    private boolean contratoVigenteHoje(Contract contract, LocalDate hoje) {
        if (contract.getDataInicio() != null && contract.getDataInicio().isAfter(hoje)) {
            return false;
        }
        if (contract.getDataTermino() != null && contract.getDataTermino().isBefore(hoje)) {
            return false;
        }
        return true;
    }
}
