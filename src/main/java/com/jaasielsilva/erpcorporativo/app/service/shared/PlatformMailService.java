package com.jaasielsilva.erpcorporativo.app.service.shared;

import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

/**
 * Serviço de envio de e-mail que lê as configurações SMTP dinamicamente
 * a partir do PlatformSettingService (banco de dados).
 *
 * Uso:
 *   platformMailService.send("destino@email.com", "Assunto", "<h1>Corpo HTML</h1>");
 */
@Service
@RequiredArgsConstructor
public class PlatformMailService {

    private static final Logger log = LoggerFactory.getLogger(PlatformMailService.class);

    private final PlatformSettingService settingService;

    /**
     * Cria um JavaMailSender sob demanda com as configurações SMTP atuais do banco.
     * Isso garante que alterações feitas no painel admin sejam aplicadas imediatamente
     * sem necessidade de reiniciar a aplicação.
     */
    private JavaMailSenderImpl buildMailSender() {
        Map<String, String> settings = settingService.asMap();

        String host = settings.getOrDefault("smtp.host", "");
        String portStr = settings.getOrDefault("smtp.port", "587");
        String username = settings.getOrDefault("smtp.username", "");
        String password = settings.getOrDefault("smtp.password", "");

        if (host.isBlank()) {
            throw new SmtpNotConfiguredException("O servidor SMTP não está configurado. Vá em Configurações → Servidor de E-mail.");
        }

        int port;
        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException e) {
            port = 587;
        }

        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.connectiontimeout", "5000");
        props.put("mail.smtp.timeout", "5000");
        props.put("mail.smtp.writetimeout", "5000");

        // Porta 465 usa SSL direto, qualquer outra usa STARTTLS
        if (port == 465) {
            props.put("mail.smtp.ssl.enable", "true");
        } else {
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "true");
        }

        return mailSender;
    }

    /**
     * Envia um e-mail HTML utilizando as configurações SMTP salvas no banco.
     *
     * @param to      endereço de destino
     * @param subject assunto do e-mail
     * @param htmlBody conteúdo HTML do corpo do e-mail
     */
    public void send(String to, String subject, String htmlBody) {
        JavaMailSenderImpl mailSender = buildMailSender();
        Map<String, String> settings = settingService.asMap();

        String fromAddress = settings.getOrDefault("smtp.from_address", mailSender.getUsername());
        String fromName = settings.getOrDefault("smtp.from_name", "ERP Corporativo");

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            helper.setFrom(fromAddress, fromName);

            mailSender.send(message);
            log.info("E-mail enviado com sucesso para: {}", to);
        } catch (MessagingException | java.io.UnsupportedEncodingException ex) {
            log.error("Falha ao enviar e-mail para {}: {}", to, ex.getMessage());
            throw new RuntimeException("Falha ao enviar e-mail: " + ex.getMessage(), ex);
        }
    }

    /**
     * Testa a conexão SMTP sem enviar nenhum e-mail de verdade.
     * Faz um handshake com o servidor SMTP e encerra.
     *
     * @return true se a conexão for bem-sucedida
     * @throws RuntimeException se falhar
     */
    public void testConnection() {
        JavaMailSenderImpl mailSender = buildMailSender();
        try {
            mailSender.testConnection();
            log.info("Teste de conexão SMTP bem-sucedido: {}:{}", mailSender.getHost(), mailSender.getPort());
        } catch (MessagingException ex) {
            log.warn("Falha no teste de conexão SMTP: {}", ex.getMessage());
            throw new RuntimeException("Falha na conexão SMTP: " + ex.getMessage(), ex);
        }
    }

    /**
     * Envia um e-mail de teste para o endereço informado.
     */
    public void sendTestEmail(String to) {
        String subject = "🧪 Teste de Configuração SMTP — ERP Corporativo";
        String body = """
                <div style="font-family: 'Segoe UI', sans-serif; max-width: 520px; margin: 0 auto; padding: 32px; background: #f8fafc; border-radius: 12px;">
                    <div style="text-align: center; margin-bottom: 24px;">
                        <div style="display: inline-block; background: linear-gradient(135deg, #6366f1, #8b5cf6); border-radius: 50%%; width: 56px; height: 56px; line-height: 56px; color: #fff; font-size: 24px;">✉</div>
                    </div>
                    <h2 style="color: #1e293b; text-align: center; margin-bottom: 8px;">Conexão SMTP Confirmada!</h2>
                    <p style="color: #64748b; text-align: center; font-size: 15px; line-height: 1.6;">
                        Se você está lendo este e-mail, significa que as configurações de SMTP da sua plataforma ERP estão
                        <strong style="color: #059669;">funcionando perfeitamente</strong>.
                    </p>
                    <hr style="border: none; border-top: 1px solid #e2e8f0; margin: 24px 0;" />
                    <p style="color: #94a3b8; font-size: 12px; text-align: center;">
                        Este é um e-mail automático de teste. Nenhuma ação é necessária.
                    </p>
                </div>
                """;
        send(to, subject, body);
    }

    /**
     * Exceção semântica para SMTP não configurado.
     */
    public static class SmtpNotConfiguredException extends RuntimeException {
        public SmtpNotConfiguredException(String message) {
            super(message);
        }
    }
}
