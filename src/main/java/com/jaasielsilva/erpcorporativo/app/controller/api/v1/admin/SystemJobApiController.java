package com.jaasielsilva.erpcorporativo.app.controller.api.v1.admin;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jaasielsilva.erpcorporativo.app.dto.api.ApiResponse;
import com.jaasielsilva.erpcorporativo.app.scheduler.MensalidadeScheduler;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/jobs")
@RequiredArgsConstructor
public class SystemJobApiController {

    private final MensalidadeScheduler mensalidadeScheduler;

    /**
     * Dispara o job de geração de mensalidades manualmente.
     * Útil para testes e reprocessamento.
     * Protegido por SUPER_ADMIN via SecurityConfig (/api/v1/admin/**).
     */
    @PostMapping("/mensalidades")
    public ApiResponse<String> dispararMensalidades() {
        mensalidadeScheduler.gerarMensalidades();
        return ApiResponse.success("Job de mensalidades executado com sucesso.");
    }
}
