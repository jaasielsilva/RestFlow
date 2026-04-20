package com.jaasielsilva.erpcorporativo.app.controller.api.v1.tenantadmin;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jaasielsilva.erpcorporativo.app.dto.api.ApiResponse;
import com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.workflow.WorkflowExecutionLogResponse;
import com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.workflow.WorkflowRuleRequest;
import com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.workflow.WorkflowRuleResponse;
import com.jaasielsilva.erpcorporativo.app.service.api.v1.tenantadmin.TenantWorkflowApiService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/tenant-admin/workflows")
@RequiredArgsConstructor
public class TenantWorkflowApiController {

    private final TenantWorkflowApiService tenantWorkflowApiService;

    @GetMapping("/rules")
    public ApiResponse<List<WorkflowRuleResponse>> listRules(Authentication authentication) {
        return ApiResponse.success(tenantWorkflowApiService.list(authentication));
    }

    @PostMapping("/rules")
    public ApiResponse<WorkflowRuleResponse> createRule(
            Authentication authentication,
            @Valid @RequestBody WorkflowRuleRequest request
    ) {
        return ApiResponse.success(tenantWorkflowApiService.create(authentication, request));
    }

    @GetMapping("/logs")
    public ApiResponse<List<WorkflowExecutionLogResponse>> logs(Authentication authentication) {
        return ApiResponse.success(tenantWorkflowApiService.logs(authentication));
    }
}
