package com.jaasielsilva.erpcorporativo.app.service.api.v1.admin;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jaasielsilva.erpcorporativo.app.dto.api.admin.compliance.LgpdRequestUpdateRequest;
import com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.compliance.LgpdRequestResponse;
import com.jaasielsilva.erpcorporativo.app.exception.ResourceNotFoundException;
import com.jaasielsilva.erpcorporativo.app.model.LgpdDataRequest;
import com.jaasielsilva.erpcorporativo.app.model.LgpdRequestStatus;
import com.jaasielsilva.erpcorporativo.app.repository.compliance.LgpdDataRequestRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LgpdAdminApiService {

    private final LgpdDataRequestRepository lgpdDataRequestRepository;

    @Transactional(readOnly = true)
    public List<LgpdRequestResponse> listOpen() {
        return lgpdDataRequestRepository.findAllByStatusOrderByCreatedAtDesc(LgpdRequestStatus.ABERTA).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public LgpdRequestResponse updateStatus(Long id, LgpdRequestUpdateRequest request) {
        LgpdDataRequest entity = lgpdDataRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitação LGPD não encontrada."));
        entity.setStatus(request.status());
        entity.setResponseNote(request.responseNote());
        return toResponse(lgpdDataRequestRepository.save(entity));
    }

    private LgpdRequestResponse toResponse(LgpdDataRequest entity) {
        return new LgpdRequestResponse(
                entity.getId(),
                entity.getRequestType(),
                entity.getStatus(),
                entity.getJustificativa(),
                entity.getResponseNote(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
