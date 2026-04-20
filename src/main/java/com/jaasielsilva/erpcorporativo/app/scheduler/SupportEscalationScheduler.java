package com.jaasielsilva.erpcorporativo.app.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.jaasielsilva.erpcorporativo.app.usecase.system.support.SupportEscalationUseCase;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SupportEscalationScheduler {

    private static final Logger log = LoggerFactory.getLogger(SupportEscalationScheduler.class);

    private final SupportEscalationUseCase supportEscalationUseCase;

    @Scheduled(cron = "0 */15 * * * ?")
    public void processEscalations() {
        SupportEscalationUseCase.EscalationResult result = supportEscalationUseCase.processEscalations();
        log.info("[SupportEscalationScheduler] Processados={}, Avisos={}, Violados={}",
                result.processed(), result.warned(), result.violated());
    }
}
