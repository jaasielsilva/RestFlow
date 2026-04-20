package com.jaasielsilva.erpcorporativo.app.service.shared;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;

@Service
public class SensitiveSettingCryptoService {

    private static final Logger log = LoggerFactory.getLogger(SensitiveSettingCryptoService.class);
    private static final String ENC_PREFIX = "{enc}";
    private static final String FIXED_SALT = "9f86d081884c7d65";

    private static final Set<String> SENSITIVE_KEYS = Set.of(
            "smtp.password",
            "billing.mp.access_token",
            "billing.mp.webhook_secret"
    );

    private final TextEncryptor encryptor;
    private final boolean enabled;

    public SensitiveSettingCryptoService(@Value("${app.settings.crypto-key:}") String cryptoKey) {
        if (cryptoKey != null && !cryptoKey.isBlank()) {
            this.encryptor = Encryptors.text(cryptoKey, FIXED_SALT);
            this.enabled = true;
        } else {
            this.encryptor = null;
            this.enabled = false;
            log.warn("Criptografia de settings sensíveis desabilitada. Defina app.settings.crypto-key.");
        }
    }

    public boolean isSensitive(String key) {
        return key != null && SENSITIVE_KEYS.contains(key);
    }

    public String encryptIfSensitive(String key, String value) {
        if (!isSensitive(key) || value == null || value.isBlank()) {
            return value;
        }
        if (!enabled) {
            return value;
        }
        if (value.startsWith(ENC_PREFIX)) {
            return value;
        }
        return ENC_PREFIX + encryptor.encrypt(value);
    }

    public String decryptIfSensitive(String key, String value) {
        if (!isSensitive(key) || value == null || value.isBlank()) {
            return value;
        }
        if (!enabled) {
            return value;
        }
        if (!value.startsWith(ENC_PREFIX)) {
            return value;
        }
        String encrypted = value.substring(ENC_PREFIX.length());
        return encryptor.decrypt(encrypted);
    }
}
