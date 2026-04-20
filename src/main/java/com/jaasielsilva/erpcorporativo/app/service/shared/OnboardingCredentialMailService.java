package com.jaasielsilva.erpcorporativo.app.service.shared;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OnboardingCredentialMailService {

    private final PlatformMailService platformMailService;
    private final PlatformSettingService platformSettingService;

    public void sendCredentials(
            String to,
            String adminNome,
            String tenantNome,
            Long tenantId,
            String emailLogin,
            String temporaryPassword
    ) {
        String loginUrl = resolveLoginUrl();
        String subject = "Acesso liberado ao ERP Corporativo";
        String body = """
                <div style="font-family: Arial, sans-serif; max-width: 620px; margin: 0 auto; color: #1f2937;">
                    <h2 style="color: #111827;">Seu acesso foi ativado com sucesso</h2>
                    <p>Olá, %s.</p>
                    <p>Recebemos a confirmação do primeiro pagamento da assinatura do tenant <strong>%s</strong>.</p>
                    <p>Credenciais iniciais:</p>
                    <ul>
                        <li><strong>URL:</strong> <a href="%s">%s</a></li>
                        <li><strong>Tenant ID:</strong> %d</li>
                        <li><strong>Login:</strong> %s</li>
                        <li><strong>Senha temporária:</strong> %s</li>
                    </ul>
                    <p style="margin-top: 16px;">Por segurança, altere essa senha no primeiro acesso.</p>
                    <p>Qualquer dúvida, responda este e-mail para o suporte.</p>
                </div>
                """.formatted(adminNome, tenantNome, loginUrl, loginUrl, tenantId, emailLogin, temporaryPassword);

        platformMailService.send(to, subject, body);
    }

    private String resolveLoginUrl() {
        String successUrl = platformSettingService.get(PlatformSettingService.MP_SUCCESS_URL, "http://localhost:8080");
        try {
            java.net.URI uri = java.net.URI.create(successUrl);
            String authority = uri.getAuthority();
            String scheme = uri.getScheme();
            if (authority != null && scheme != null) {
                return scheme + "://" + authority + "/login";
            }
            return "http://localhost:8080/login";
        } catch (RuntimeException ex) {
            return "http://localhost:8080/login";
        }
    }
}
