package com.jaasielsilva.erpcorporativo.app.controller.api.v1.admin;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.jaasielsilva.erpcorporativo.app.dto.api.ApiResponse;
import com.jaasielsilva.erpcorporativo.app.dto.api.admin.plan.AssignPlanRequest;
import com.jaasielsilva.erpcorporativo.app.dto.api.admin.plan.SubscriptionPlanRequest;
import com.jaasielsilva.erpcorporativo.app.dto.api.admin.plan.SubscriptionPlanResponse;
import com.jaasielsilva.erpcorporativo.app.service.api.v1.admin.SubscriptionPlanAdminApiService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/plans")
@RequiredArgsConstructor
public class SubscriptionPlanAdminApiController {

    private final SubscriptionPlanAdminApiService subscriptionPlanAdminApiService;

    @GetMapping
    public ApiResponse<List<SubscriptionPlanResponse>> listAll() {
        return ApiResponse.success(subscriptionPlanAdminApiService.listAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<SubscriptionPlanResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(subscriptionPlanAdminApiService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SubscriptionPlanResponse> create(@Valid @RequestBody SubscriptionPlanRequest request) {
        return ApiResponse.success(subscriptionPlanAdminApiService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<SubscriptionPlanResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody SubscriptionPlanRequest request
    ) {
        return ApiResponse.success(subscriptionPlanAdminApiService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        subscriptionPlanAdminApiService.delete(id);
    }

    @PostMapping("/tenants/{tenantId}/assign")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void assignPlanToTenant(
            @PathVariable Long tenantId,
            @Valid @RequestBody AssignPlanRequest request
    ) {
        subscriptionPlanAdminApiService.assignPlanToTenant(tenantId, request);
    }

    @DeleteMapping("/tenants/{tenantId}/assign")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removePlanFromTenant(@PathVariable Long tenantId) {
        subscriptionPlanAdminApiService.removePlanFromTenant(tenantId);
    }
}
