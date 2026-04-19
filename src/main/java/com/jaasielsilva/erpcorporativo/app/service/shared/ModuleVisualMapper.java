package com.jaasielsilva.erpcorporativo.app.service.shared;

import java.util.Locale;

import org.springframework.stereotype.Component;

@Component("moduleVisualMapper")
public class ModuleVisualMapper {

    public String iconClass(String codigo) {
        String normalized = normalize(codigo);
        return switch (normalized) {
            case "dashboard" -> "fa-solid fa-house";
            case "usuarios" -> "fa-solid fa-users";
            case "configuracoes" -> "fa-solid fa-gear";
            case "pedidos" -> "fa-solid fa-receipt";
            case "estoque" -> "fa-solid fa-boxes-stacked";
            case "financeiro" -> "fa-solid fa-wallet";
            case "relatorios" -> "fa-solid fa-chart-column";
            case "conhecimento", "base_conhecimento" -> "fa-solid fa-book-open";
            default -> "fa-solid fa-puzzle-piece";
        };
    }

    public String toneClass(String codigo) {
        String normalized = normalize(codigo);
        return switch (normalized) {
            case "dashboard" -> "module-tone-blue";
            case "usuarios" -> "module-tone-green";
            case "configuracoes" -> "module-tone-slate";
            case "pedidos" -> "module-tone-orange";
            case "estoque" -> "module-tone-purple";
            case "financeiro" -> "module-tone-amber";
            case "relatorios" -> "module-tone-cyan";
            case "conhecimento", "base_conhecimento" -> "module-tone-green";
            default -> "module-tone-indigo";
        };
    }

    private String normalize(String codigo) {
        if (codigo == null) {
            return "";
        }
        return codigo.trim().toLowerCase(Locale.ROOT);
    }
}
