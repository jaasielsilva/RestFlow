package com.jaasielsilva.erpcorporativo.app.repository.support;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jaasielsilva.erpcorporativo.app.model.SupportMessage;

public interface SupportMessageRepository extends JpaRepository<SupportMessage, Long> {

    List<SupportMessage> findAllByTicketIdOrderByCreatedAtAsc(Long ticketId);
}
