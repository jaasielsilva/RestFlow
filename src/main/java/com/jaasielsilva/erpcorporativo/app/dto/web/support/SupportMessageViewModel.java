package com.jaasielsilva.erpcorporativo.app.dto.web.support;

import java.time.LocalDateTime;
import java.util.List;

import com.jaasielsilva.erpcorporativo.app.model.SupportMessageVisibility;

public record SupportMessageViewModel(
        Long id,
        String autorNome,
        String conteudo,
        SupportMessageVisibility visibilidade,
        LocalDateTime createdAt,
        List<SupportAttachmentViewModel> anexos
) {
}
