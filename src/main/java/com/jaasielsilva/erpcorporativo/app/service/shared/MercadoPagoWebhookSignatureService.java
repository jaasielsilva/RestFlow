package com.jaasielsilva.erpcorporativo.app.service.shared;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MercadoPagoWebhookSignatureService {

    private final PlatformSettingService settingService;

    public SignatureVerificationResult verify(String xSignature, String xRequestId, String dataIdQueryParam) {
        if (xSignature == null || xSignature.isBlank()) {
            return SignatureVerificationResult.fail("Header x-signature ausente.");
        }

        String secret = settingService.get(PlatformSettingService.MP_WEBHOOK_SECRET, "");
        if (secret == null || secret.isBlank()) {
            return SignatureVerificationResult.fail(
                    "Configuração ausente: billing.mp.webhook_secret.");
        }

        ParsedSignature parsedSignature = parseSignature(xSignature);
        if (parsedSignature == null || parsedSignature.ts().isBlank() || parsedSignature.v1().isBlank()) {
            return SignatureVerificationResult.fail("Header x-signature inválido.");
        }

        String manifest = buildManifest(dataIdQueryParam, xRequestId, parsedSignature.ts());
        if (manifest.isBlank()) {
            return SignatureVerificationResult.fail("Manifest de assinatura vazio.");
        }

        String expectedHash = hmacSha256Hex(manifest, secret);
        if (expectedHash == null) {
            return SignatureVerificationResult.fail("Falha ao calcular HMAC da assinatura.");
        }

        boolean valid = MessageDigest.isEqual(
                expectedHash.getBytes(StandardCharsets.UTF_8),
                parsedSignature.v1().toLowerCase(Locale.ROOT).getBytes(StandardCharsets.UTF_8)
        );
        if (!valid) {
            return SignatureVerificationResult.fail("Assinatura divergente.");
        }
        return SignatureVerificationResult.ok();
    }

    private ParsedSignature parseSignature(String xSignature) {
        String ts = null;
        String v1 = null;
        String[] parts = xSignature.split(",");
        for (String part : parts) {
            String[] keyValue = part.split("=", 2);
            if (keyValue.length != 2) {
                continue;
            }
            String key = keyValue[0].trim().toLowerCase(Locale.ROOT);
            String value = keyValue[1].trim();
            if ("ts".equals(key)) {
                ts = value;
            } else if ("v1".equals(key)) {
                v1 = value;
            }
        }
        if (ts == null || v1 == null) {
            return null;
        }
        return new ParsedSignature(ts, v1);
    }

    private String buildManifest(String dataIdQueryParam, String xRequestId, String ts) {
        List<String> sections = new ArrayList<>();
        if (dataIdQueryParam != null && !dataIdQueryParam.isBlank()) {
            sections.add("id:" + dataIdQueryParam.toLowerCase(Locale.ROOT));
        }
        if (xRequestId != null && !xRequestId.isBlank()) {
            sections.add("request-id:" + xRequestId);
        }
        if (ts != null && !ts.isBlank()) {
            sections.add("ts:" + ts);
        }

        StringBuilder manifest = new StringBuilder();
        for (String section : sections) {
            manifest.append(section).append(";");
        }
        return manifest.toString();
    }

    private String hmacSha256Hex(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return toHex(hash);
        } catch (Exception ex) {
            return null;
        }
    }

    private String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private record ParsedSignature(String ts, String v1) {
    }

    public record SignatureVerificationResult(boolean valid, String reason) {
        public static SignatureVerificationResult ok() {
            return new SignatureVerificationResult(true, null);
        }

        public static SignatureVerificationResult fail(String reason) {
            return new SignatureVerificationResult(false, reason);
        }
    }
}
