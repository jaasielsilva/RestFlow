package com.jaasielsilva.erpcorporativo.app.scheduler;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.jaasielsilva.erpcorporativo.app.model.PaymentProvider;
import com.jaasielsilva.erpcorporativo.app.model.PaymentStatus;
import com.jaasielsilva.erpcorporativo.app.repository.contract.PaymentRecordRepository;
import com.jaasielsilva.erpcorporativo.app.service.shared.AdminNotificationService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MercadoPagoNotificationScheduler {

    private final PaymentRecordRepository paymentRecordRepository;
    private final AdminNotificationService adminNotificationService;

    /**
     * Job leve para criar notificações no sino quando houver pagamento confirmado.
     */
    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void processPaidPayments() {
        var records = paymentRecordRepository
                .findTop30ByStatusAndPaymentProviderAndProviderPaymentIdIsNotNullAndPaymentNotifiedAtIsNullOrderByUpdatedAtDesc(
                        PaymentStatus.PAGO,
                        PaymentProvider.MERCADO_PAGO
                );

        for (var record : records) {
            adminNotificationService.createPaymentReceivedNotifications(record);
            record.setPaymentNotifiedAt(LocalDateTime.now());
            paymentRecordRepository.save(record);
        }
    }
}
