package com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.support;

import java.time.LocalDateTime;
import java.util.List;

import com.jaasielsilva.erpcorporativo.app.model.SupportMessageVisibility;

public record SupportMessageResponse(
        Long id,
        String autorNome,
        String conteudo,
        SupportMessageVisibility visibilidade,
        LocalDateTime createdAt,
        List<SupportAttachmentResponse> anexos
) {
}
