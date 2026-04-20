package com.jaasielsilva.erpcorporativo.app.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.jaasielsilva.erpcorporativo.app.service.shared.WorkflowAutomationEngineService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WorkflowAutomationScheduler {

    private static final Logger log = LoggerFactory.getLogger(WorkflowAutomationScheduler.class);

    private final WorkflowAutomationEngineService workflowAutomationEngineService;

    @Scheduled(cron = "0 */20 * * * ?")
    public void run() {
        int executed = workflowAutomationEngineService.runScheduledAutomations();
        log.info("[WorkflowAutomationScheduler] Execuções de regras: {}", executed);
    }
}
