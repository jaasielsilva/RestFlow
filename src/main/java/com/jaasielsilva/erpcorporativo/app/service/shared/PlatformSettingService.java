package com.jaasielsilva.erpcorporativo.app.service.shared;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jaasielsilva.erpcorporativo.app.model.AuditAction;
import com.jaasielsilva.erpcorporativo.app.model.PlatformSetting;
import com.jaasielsilva.erpcorporativo.app.repository.settings.PlatformSettingRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlatformSettingService {

    private final PlatformSettingRepository settingRepository;
    private final AuditService auditService;
    private final SensitiveSettingCryptoService sensitiveSettingCryptoService;

    // Chaves padrão
    public static final String PLATFORM_NAME      = "platform.name";
    public static final String SUPPORT_EMAIL      = "support.email";
    public static final String MAX_USERS_PER_PLAN = "plan.max_users_default";
    public static final String SESSION_TIMEOUT    = "security.session_timeout_minutes";
    public static final String PASSWORD_MIN_LEN   = "security.password_min_length";
    public static final String MAINTENANCE_MODE   = "platform.maintenance_mode";
    public static final String MP_ACCESS_TOKEN    = "billing.mp.access_token";
    public static final String MP_PUBLIC_KEY      = "billing.mp.public_key";
    public static final String MP_WEBHOOK_SECRET  = "billing.mp.webhook_secret";
    public static final String MP_SUCCESS_URL     = "billing.mp.success_url";
    public static final String MP_FAILURE_URL     = "billing.mp.failure_url";
    public static final String MP_PENDING_URL     = "billing.mp.pending_url";
    public static final String ONBOARDING_ENTRY_PRICE = "billing.onboarding.entry_price";

    @Transactional(readOnly = true)
    public List<PlatformSetting> findAll() {
        return settingRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Map<String, String> asMap() {
        return settingRepository.findAll().stream()
                .collect(Collectors.toMap(
                        PlatformSetting::getChave,
                        s -> sensitiveSettingCryptoService.decryptIfSensitive(s.getChave(), s.getValor())
                ));
    }

    @Transactional(readOnly = true)
    public String get(String chave, String defaultValue) {
        return settingRepository.findByChave(chave)
                .map(s -> sensitiveSettingCryptoService.decryptIfSensitive(chave, s.getValor()))
                .orElse(defaultValue);
    }

    @Transactional
    public void set(String chave, String valor, String executadoPor) {
        PlatformSetting setting = settingRepository.findByChave(chave)
                .orElseGet(() -> PlatformSetting.builder().chave(chave).descricao(chave).build());
        setting.setValor(sensitiveSettingCryptoService.encryptIfSensitive(chave, valor));
        settingRepository.save(setting);
        String auditValue = sensitiveSettingCryptoService.isSensitive(chave) ? "[PROTEGIDO]" : String.valueOf(valor);
        auditService.log(AuditAction.CONFIGURACAO_ATUALIZADA,
                "Configuração '" + chave + "' atualizada para '" + auditValue + "'.", executadoPor);
    }

    @Transactional
    public void saveAll(Map<String, String> values, String executadoPor) {
        values.forEach((chave, valor) -> set(chave, valor, executadoPor));
    }

    /** Garante que as configurações padrão existam no banco ao iniciar */
    @Transactional
    public void ensureDefaults() {
        ensureDefault(PLATFORM_NAME,      "ERP Corporativo",  "Nome da plataforma");
        ensureDefault(SUPPORT_EMAIL,      "suporte@erp.com",  "E-mail de suporte");
        ensureDefault(MAX_USERS_PER_PLAN, "50",               "Máximo de usuários por plano (padrão)");
        ensureDefault(SESSION_TIMEOUT,    "60",               "Timeout de sessão em minutos");
        ensureDefault(PASSWORD_MIN_LEN,   "8",                "Tamanho mínimo de senha");
        ensureDefault(MAINTENANCE_MODE,   "false",            "Modo de manutenção da plataforma");
        ensureDefault(MP_ACCESS_TOKEN,    "",                 "Token de acesso Mercado Pago");
        ensureDefault(MP_PUBLIC_KEY,      "",                 "Chave pública Mercado Pago");
        ensureDefault(MP_WEBHOOK_SECRET,  "",                 "Segredo de assinatura de webhook Mercado Pago");
        ensureDefault(MP_SUCCESS_URL,     "http://localhost:8080/app/minha-conta/faturas", "URL de retorno sucesso checkout");
        ensureDefault(MP_FAILURE_URL,     "http://localhost:8080/app/minha-conta/faturas", "URL de retorno falha checkout");
        ensureDefault(MP_PENDING_URL,     "http://localhost:8080/app/minha-conta/faturas", "URL de retorno pendente checkout");
        ensureDefault(ONBOARDING_ENTRY_PRICE, "2.00",         "Preço de entrada para onboarding self-service");
    }

    private void ensureDefault(String chave, String valor, String descricao) {
        if (settingRepository.findByChave(chave).isEmpty()) {
            settingRepository.save(PlatformSetting.builder()
                    .chave(chave).valor(valor).descricao(descricao).build());
        }
    }
}
