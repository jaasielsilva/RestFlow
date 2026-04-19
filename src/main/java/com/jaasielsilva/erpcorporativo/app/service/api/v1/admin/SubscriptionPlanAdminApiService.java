package com.jaasielsilva.erpcorporativo.app.service.api.v1.admin;

import java.util.List;

import org.springframework.stereotype.Service;

import com.jaasielsilva.erpcorporativo.app.dto.api.admin.plan.AssignPlanRequest;
import com.jaasielsilva.erpcorporativo.app.dto.api.admin.plan.SubscriptionPlanRequest;
import com.jaasielsilva.erpcorporativo.app.dto.api.admin.plan.SubscriptionPlanResponse;
import com.jaasielsilva.erpcorporativo.app.model.SubscriptionPlan;
import com.jaasielsilva.erpcorporativo.app.usecase.api.v1.admin.SubscriptionPlanAdminUseCase;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubscriptionPlanAdminApiService {

    private final SubscriptionPlanAdminUseCase subscriptionPlanAdminUseCase;

    public List<SubscriptionPlanResponse> listAll() {
        return subscriptionPlanAdminUseCase.listAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public SubscriptionPlanResponse getById(Long id) {
        return toResponse(subscriptionPlanAdminUseCase.getById(id));
    }

    public SubscriptionPlanResponse create(SubscriptionPlanRequest request) {
        return toResponse(subscriptionPlanAdminUseCase.create(
                request.codigo(),
                request.nome(),
                request.descricao(),
                request.ativo(),
                request.moduleIds()
        ));
    }

    public SubscriptionPlanResponse update(Long id, SubscriptionPlanRequest request) {
        return toResponse(subscriptionPlanAdminUseCase.update(
                id,
                request.codigo(),
                request.nome(),
                request.descricao(),
                request.ativo(),
                request.moduleIds()
        ));
    }

    public void delete(Long id) {
        subscriptionPlanAdminUseCase.delete(id);
    }

    public void assignPlanToTenant(Long tenantId, AssignPlanRequest request) {
        subscriptionPlanAdminUseCase.assignPlanToTenant(tenantId, request.planId());
    }

    public void removePlanFromTenant(Long tenantId) {
        subscriptionPlanAdminUseCase.removePlanFromTenant(tenantId);
    }

    private SubscriptionPlanResponse toResponse(SubscriptionPlan plan) {
        List<SubscriptionPlanResponse.PlanModuleItem> moduleItems = plan.getModules().stream()
                .sorted((a, b) -> a.getNome().compareToIgnoreCase(b.getNome()))
                .map(m -> new SubscriptionPlanResponse.PlanModuleItem(m.getId(), m.getCodigo(), m.getNome()))
                .toList();

        return new SubscriptionPlanResponse(
                plan.getId(),
                plan.getCodigo(),
                plan.getNome(),
                plan.getDescricao(),
                plan.isAtivo(),
                moduleItems,
                plan.getCreatedAt()
        );
    }
}
