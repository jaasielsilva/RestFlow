package com.jaasielsilva.erpcorporativo.app.controller.api.v1.tenantadmin;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.jaasielsilva.erpcorporativo.app.dto.api.ApiResponse;
import com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.support.SupportDashboardResponse;
import com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.support.SupportSlaPolicyRequest;
import com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.support.SupportSlaPolicyResponse;
import com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.support.SupportTicketFilter;
import com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.support.SupportTicketMessageRequest;
import com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.support.SupportTicketPageResponse;
import com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.support.SupportTicketRequest;
import com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.support.SupportTicketResponse;
import com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.support.SupportTicketStatusRequest;
import com.jaasielsilva.erpcorporativo.app.model.SupportSlaState;
import com.jaasielsilva.erpcorporativo.app.model.SupportTicketPriority;
import com.jaasielsilva.erpcorporativo.app.model.SupportTicketStatus;
import com.jaasielsilva.erpcorporativo.app.service.api.v1.tenantadmin.SupportTenantApiService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/tenant-admin/support")
@RequiredArgsConstructor
public class SupportTenantApiController {

    private final SupportTenantApiService supportTenantApiService;

    @GetMapping("/tickets")
    public ApiResponse<SupportTicketPageResponse> list(
            Authentication authentication,
            @RequestParam(name = "assunto", required = false) String assunto,
            @RequestParam(name = "categoria", required = false) String categoria,
            @RequestParam(name = "status", required = false) SupportTicketStatus status,
            @RequestParam(name = "prioridade", required = false) SupportTicketPriority prioridade,
            @RequestParam(name = "sla", required = false) SupportSlaState slaState,
            @RequestParam(name = "clienteId", required = false) Long clienteId,
            @RequestParam(name = "responsavelId", required = false) Long responsavelId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size
    ) {
        return ApiResponse.success(supportTenantApiService.list(
                authentication,
                new SupportTicketFilter(assunto, categoria, status, prioridade, slaState, clienteId, responsavelId),
                page,
                size
        ));
    }

    @GetMapping("/tickets/{id}")
    public ApiResponse<SupportTicketResponse> getById(Authentication authentication, @PathVariable("id") Long id) {
        return ApiResponse.success(supportTenantApiService.getById(authentication, id));
    }

    @PostMapping("/tickets")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SupportTicketResponse> create(
            Authentication authentication,
            @Valid @RequestBody SupportTicketRequest request
    ) {
        return ApiResponse.success(supportTenantApiService.create(authentication, request));
    }

    @PutMapping("/tickets/{id}")
    public ApiResponse<SupportTicketResponse> update(
            Authentication authentication,
            @PathVariable("id") Long id,
            @Valid @RequestBody SupportTicketRequest request
    ) {
        return ApiResponse.success(supportTenantApiService.update(authentication, id, request));
    }

    @PostMapping("/tickets/{id}/status")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateStatus(
            Authentication authentication,
            @PathVariable("id") Long id,
            @Valid @RequestBody SupportTicketStatusRequest request
    ) {
        supportTenantApiService.updateStatus(authentication, id, request.status());
    }

    @PostMapping("/tickets/{id}/messages")
    public ApiResponse<SupportTicketResponse> addMessage(
            Authentication authentication,
            @PathVariable("id") Long id,
            @Valid @RequestBody SupportTicketMessageRequest request
    ) {
        return ApiResponse.success(supportTenantApiService.addMessage(authentication, id, request));
    }

    @GetMapping("/sla")
    public ApiResponse<SupportSlaPolicyResponse> getSlaPolicy(Authentication authentication) {
        return ApiResponse.success(supportTenantApiService.getSlaPolicy(authentication));
    }

    @PutMapping("/sla")
    public ApiResponse<SupportSlaPolicyResponse> updateSlaPolicy(
            Authentication authentication,
            @Valid @RequestBody SupportSlaPolicyRequest request
    ) {
        return ApiResponse.success(supportTenantApiService.updateSlaPolicy(authentication, request));
    }

    @GetMapping("/dashboard")
    public ApiResponse<SupportDashboardResponse> dashboard(
            Authentication authentication,
            @RequestParam(name = "periodDays", defaultValue = "30") int periodDays
    ) {
        return ApiResponse.success(supportTenantApiService.dashboard(authentication, periodDays));
    }
}
