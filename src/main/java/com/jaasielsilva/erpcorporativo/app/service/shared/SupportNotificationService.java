package com.jaasielsilva.erpcorporativo.app.service.shared;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jaasielsilva.erpcorporativo.app.model.SupportMessage;
import com.jaasielsilva.erpcorporativo.app.model.SupportTicket;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SupportNotificationService {

    private static final Logger log = LoggerFactory.getLogger(SupportNotificationService.class);

    private final PlatformMailService platformMailService;
    private final PlatformSettingService settingService;

    public void notifyTicketCreated(SupportTicket ticket) {
        String recipient = resolveRecipient(ticket);
        if (recipient == null) {
            return;
        }
        String subject = "[Suporte] Novo chamado " + ticket.getNumero();
        String body = """
                <h3>Novo chamado registrado</h3>
                <p><strong>Número:</strong> %s</p>
                <p><strong>Assunto:</strong> %s</p>
                <p><strong>Prioridade:</strong> %s</p>
                <p><strong>Status:</strong> %s</p>
                """.formatted(ticket.getNumero(), ticket.getAssunto(), ticket.getPrioridade(), ticket.getStatus());
        safeSend(recipient, subject, body);
    }

    public void notifyMessageAdded(SupportTicket ticket, SupportMessage message) {
        String recipient = resolveRecipient(ticket);
        if (recipient == null) {
            return;
        }
        String subject = "[Suporte] Atualização no chamado " + ticket.getNumero();
        String body = """
                <h3>Novo comentário no chamado %s</h3>
                <p><strong>Autor:</strong> %s</p>
                <p><strong>Visibilidade:</strong> %s</p>
                <p>%s</p>
                """.formatted(
                ticket.getNumero(),
                message.getAutorNome() != null ? message.getAutorNome() : "Sistema",
                message.getVisibilidade(),
                message.getConteudo()
        );
        safeSend(recipient, subject, body);
    }

    private String resolveRecipient(SupportTicket ticket) {
        if (ticket.getSolicitanteEmail() != null && !ticket.getSolicitanteEmail().isBlank()) {
            return ticket.getSolicitanteEmail();
        }
        String configured = settingService.get(PlatformSettingService.SUPPORT_EMAIL, null);
        if (configured == null || configured.isBlank()) {
            return null;
        }
        return configured;
    }

    private void safeSend(String recipient, String subject, String body) {
        try {
            platformMailService.send(recipient, subject, body);
        } catch (PlatformMailService.SmtpNotConfiguredException ex) {
            log.warn("Notificação de suporte ignorada por SMTP não configurado: {}", ex.getMessage());
        } catch (RuntimeException ex) {
            log.warn("Falha ao enviar notificação de suporte para {}: {}", recipient, ex.getMessage());
        }
    }
}
