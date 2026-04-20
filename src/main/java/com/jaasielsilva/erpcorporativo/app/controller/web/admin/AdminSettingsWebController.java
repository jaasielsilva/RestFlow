package com.jaasielsilva.erpcorporativo.app.controller.web.admin;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.jaasielsilva.erpcorporativo.app.service.shared.PlatformMailService;
import com.jaasielsilva.erpcorporativo.app.service.shared.PlatformSettingService;
import com.jaasielsilva.erpcorporativo.app.service.shared.MercadoPagoBillingService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/settings")
@RequiredArgsConstructor
public class AdminSettingsWebController {

    private static final Set<String> ALLOWED_KEYS = Set.of(
            PlatformSettingService.PLATFORM_NAME,
            PlatformSettingService.SUPPORT_EMAIL,
            PlatformSettingService.MAX_USERS_PER_PLAN,
            PlatformSettingService.SESSION_TIMEOUT,
            PlatformSettingService.PASSWORD_MIN_LEN,
            PlatformSettingService.MAINTENANCE_MODE,
            "smtp.host",
            "smtp.port",
            "smtp.username",
            "smtp.password",
            "smtp.from_address",
            "smtp.from_name",
            PlatformSettingService.MP_ACCESS_TOKEN,
            PlatformSettingService.MP_PUBLIC_KEY,
            PlatformSettingService.MP_WEBHOOK_SECRET,
            PlatformSettingService.MP_SUCCESS_URL,
            PlatformSettingService.MP_FAILURE_URL,
            PlatformSettingService.MP_PENDING_URL
    );

    private static final Set<String> SENSITIVE_KEYS = Set.of(
            "smtp.password",
            PlatformSettingService.MP_ACCESS_TOKEN,
            PlatformSettingService.MP_WEBHOOK_SECRET
    );

    private static final Map<String, List<String>> SECTION_KEYS = Map.of(
            "geral", List.of(
                    PlatformSettingService.PLATFORM_NAME,
                    PlatformSettingService.SUPPORT_EMAIL,
                    PlatformSettingService.MAINTENANCE_MODE
            ),
            "smtp", List.of(
                    "smtp.host",
                    "smtp.port",
                    "smtp.username",
                    "smtp.password",
                    "smtp.from_address",
                    "smtp.from_name"
            ),
            "billing", List.of(
                    PlatformSettingService.MP_ACCESS_TOKEN,
                    PlatformSettingService.MP_PUBLIC_KEY,
                    PlatformSettingService.MP_WEBHOOK_SECRET,
                    PlatformSettingService.MP_SUCCESS_URL,
                    PlatformSettingService.MP_FAILURE_URL,
                    PlatformSettingService.MP_PENDING_URL
            ),
            "security", List.of(
                    PlatformSettingService.SESSION_TIMEOUT,
                    PlatformSettingService.PASSWORD_MIN_LEN,
                    PlatformSettingService.MAX_USERS_PER_PLAN
            )
    );

    private final PlatformSettingService platformSettingService;
    private final PlatformMailService platformMailService;
    private final MercadoPagoBillingService mercadoPagoBillingService;

    @GetMapping
    public String index(Model model) {
        Map<String, String> settings = platformSettingService.asMap();
        model.addAttribute("settings", settings);
        model.addAttribute("hasSmtpPassword", isConfigured(settings.get("smtp.password")));
        model.addAttribute("hasMpAccessToken", isConfigured(settings.get(PlatformSettingService.MP_ACCESS_TOKEN)));
        model.addAttribute("hasMpWebhookSecret", isConfigured(settings.get(PlatformSettingService.MP_WEBHOOK_SECRET)));
        model.addAttribute("activeMenu", "settings");
        model.addAttribute("pageTitle", "Configurações");
        model.addAttribute("pageSubtitle", "Parâmetros globais da plataforma");
        return "admin/settings/index";
    }

    @PostMapping
    public String save(
            @RequestParam Map<String, String> params,
            @RequestParam(name = "section", required = false) String section,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        String hash = resolveHash(section);
        try {
            params.remove("_csrf");
            Set<String> targetKeys = resolveTargetKeys(section);
            Map<String, String> filtered = new LinkedHashMap<>();
            for (String key : targetKeys) {
                if (!params.containsKey(key)) {
                    continue;
                }
                String value = params.get(key);
                if (value != null) {
                    value = value.trim();
                }
                if (SENSITIVE_KEYS.contains(key) && (value == null || value.isBlank())) {
                    continue;
                }
                filtered.put(key, value);
            }

            validateBoundaries(filtered);
            platformSettingService.saveAll(filtered, authentication.getName());
            redirectAttributes.addFlashAttribute("toastSuccess", "Configurações salvas com sucesso.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("toastError", ex.getMessage());
        }
        return "redirect:/admin/settings" + hash;
    }

    @PostMapping("/smtp/test")
    public String testSmtp(
            @RequestParam(name = "testEmail", required = false) String testEmail,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        String to = (testEmail == null || testEmail.isBlank()) ? authentication.getName() : testEmail.trim();
        try {
            platformMailService.testConnection();
            platformMailService.sendTestEmail(to);
            redirectAttributes.addFlashAttribute("toastSuccess", "E-mail de teste enviado para " + to + ".");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("toastError", "Falha no teste SMTP: " + ex.getMessage());
        }
        return "redirect:/admin/settings#v-pills-smtp";
    }

    @PostMapping("/billing/test")
    public String testMercadoPago(RedirectAttributes redirectAttributes) {
        try {
            String accountName = mercadoPagoBillingService.testCredentials();
            redirectAttributes.addFlashAttribute("toastSuccess", "Mercado Pago conectado com sucesso: " + accountName + ".");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("toastError", "Falha ao validar Mercado Pago: " + ex.getMessage());
        }
        return "redirect:/admin/settings#v-pills-billing";
    }

    private boolean isConfigured(String value) {
        return value != null && !value.isBlank();
    }

    private void validateBoundaries(Map<String, String> params) {
        validateIntegerRangeIfPresent(params, PlatformSettingService.SESSION_TIMEOUT, 5, 1440,
                "Timeout de sessão deve estar entre 5 e 1440 minutos.");
        validateIntegerRangeIfPresent(params, PlatformSettingService.PASSWORD_MIN_LEN, 6, 32,
                "Tamanho mínimo da senha deve estar entre 6 e 32.");
        validateIntegerRangeIfPresent(params, PlatformSettingService.MAX_USERS_PER_PLAN, 1, 100000,
                "Máximo de usuários padrão deve ser maior que zero.");
    }

    private void validateIntegerRangeIfPresent(
            Map<String, String> params,
            String key,
            int min,
            int max,
            String message
    ) {
        if (!params.containsKey(key)) {
            return;
        }
        String rawValue = params.get(key);
        if (rawValue == null || rawValue.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        try {
            int value = Integer.parseInt(rawValue);
            if (value < min || value > max) {
                throw new IllegalArgumentException(message);
            }
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(message);
        }
    }

    private Set<String> resolveTargetKeys(String section) {
        if (section == null || section.isBlank()) {
            return ALLOWED_KEYS;
        }
        List<String> keys = SECTION_KEYS.get(section.toLowerCase());
        if (keys == null) {
            throw new IllegalArgumentException("Seção inválida para salvar configurações.");
        }
        return Set.copyOf(keys);
    }

    private String resolveHash(String section) {
        if (section == null || section.isBlank()) {
            return "";
        }
        return switch (section.toLowerCase()) {
            case "geral" -> "#v-pills-geral";
            case "smtp" -> "#v-pills-smtp";
            case "billing" -> "#v-pills-billing";
            case "security" -> "#v-pills-security";
            default -> "";
        };
    }
}
