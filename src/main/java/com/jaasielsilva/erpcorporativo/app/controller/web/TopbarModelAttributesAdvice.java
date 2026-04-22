package com.jaasielsilva.erpcorporativo.app.controller.web;

import java.util.Arrays;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.jaasielsilva.erpcorporativo.app.repository.usuario.UsuarioRepository;
import com.jaasielsilva.erpcorporativo.app.security.AppUserDetails;

import lombok.RequiredArgsConstructor;

@ControllerAdvice(annotations = Controller.class)
@RequiredArgsConstructor
public class TopbarModelAttributesAdvice {

    private final UsuarioRepository usuarioRepository;

    @ModelAttribute("topbarUserName")
    public String topbarUserName(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AppUserDetails userDetails)) {
            return "Usuário";
        }

        return usuarioRepository.findById(userDetails.getUsuarioId())
                .map(usuario -> usuario.getNome())
                .filter(StringUtils::hasText)
                .map(String::trim)
                .orElseGet(() -> authentication.getName());
    }

    @ModelAttribute("topbarUserInitials")
    public String topbarUserInitials(@ModelAttribute("topbarUserName") String topbarUserName) {
        return resolveInitials(topbarUserName);
    }

    @ModelAttribute("topbarAvatarRoleClass")
    public String topbarAvatarRoleClass(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AppUserDetails userDetails)) {
            return "avatar-role-user";
        }
        return switch (userDetails.getRole()) {
            case SUPER_ADMIN -> "avatar-role-super-admin";
            case ADMIN -> "avatar-role-admin";
            case USER -> "avatar-role-user";
        };
    }

    private String resolveInitials(String fullName) {
        if (!StringUtils.hasText(fullName)) {
            return "U";
        }

        String[] parts = Arrays.stream(fullName.trim().split("\\s+"))
                .filter(StringUtils::hasText)
                .toArray(String[]::new);

        if (parts.length == 0) {
            return "U";
        }

        if (parts.length == 1) {
            return firstLetter(parts[0]);
        }

        return firstLetter(parts[0]) + firstLetter(parts[parts.length - 1]);
    }

    private String firstLetter(String text) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        return text.substring(0, 1).toUpperCase();
    }
}
