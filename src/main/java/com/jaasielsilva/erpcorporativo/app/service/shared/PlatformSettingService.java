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

    // Chaves padrão
    public static final String PLATFORM_NAME      = "platform.name";
    public static final String SUPPORT_EMAIL      = "support.email";
    public static final String MAX_USERS_PER_PLAN = "plan.max_users_default";
    public static final String SESSION_TIMEOUT    = "security.session_timeout_minutes";
    public static final String PASSWORD_MIN_LEN   = "security.password_min_length";
    public static final String MAINTENANCE_MODE   = "platform.maintenance_mode";

    @Transactional(readOnly = true)
    public List<PlatformSetting> findAll() {
        return settingRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Map<String, String> asMap() {
        return settingRepository.findAll().stream()
                .collect(Collectors.toMap(PlatformSetting::getChave, PlatformSetting::getValor));
    }

    @Transactional(readOnly = true)
    public String get(String chave, String defaultValue) {
        return settingRepository.findByChave(chave)
                .map(PlatformSetting::getValor)
                .orElse(defaultValue);
    }

    @Transactional
    public void set(String chave, String valor, String executadoPor) {
        PlatformSetting setting = settingRepository.findByChave(chave)
                .orElseGet(() -> PlatformSetting.builder().chave(chave).descricao(chave).build());
        setting.setValor(valor);
        settingRepository.save(setting);
        auditService.log(AuditAction.CONFIGURACAO_ATUALIZADA,
                "Configuração '" + chave + "' atualizada para '" + valor + "'.", executadoPor);
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
    }

    private void ensureDefault(String chave, String valor, String descricao) {
        if (settingRepository.findByChave(chave).isEmpty()) {
            settingRepository.save(PlatformSetting.builder()
                    .chave(chave).valor(valor).descricao(descricao).build());
        }
    }
}
