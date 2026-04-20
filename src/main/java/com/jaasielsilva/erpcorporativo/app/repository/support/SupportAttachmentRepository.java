package com.jaasielsilva.erpcorporativo.app.repository.support;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jaasielsilva.erpcorporativo.app.model.SupportAttachment;

public interface SupportAttachmentRepository extends JpaRepository<SupportAttachment, Long> {

    List<SupportAttachment> findAllByTicketIdOrderByCreatedAtAsc(Long ticketId);

    List<SupportAttachment> findAllByMessageIdOrderByCreatedAtAsc(Long messageId);
}
