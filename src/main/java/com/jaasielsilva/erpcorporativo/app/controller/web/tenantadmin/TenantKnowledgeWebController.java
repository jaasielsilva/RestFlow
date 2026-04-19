package com.jaasielsilva.erpcorporativo.app.controller.web.tenantadmin;

import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.jaasielsilva.erpcorporativo.app.model.KnowledgeArticle;
import com.jaasielsilva.erpcorporativo.app.repository.knowledge.KnowledgeArticleRepository;
import com.jaasielsilva.erpcorporativo.app.security.AppUserDetails;
import com.jaasielsilva.erpcorporativo.app.security.SecurityPrincipalUtils;
import com.jaasielsilva.erpcorporativo.app.service.web.tenantadmin.TenantPortalWebService;
import com.jaasielsilva.erpcorporativo.app.usecase.web.tenantadmin.KnowledgeTenantUseCase;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/app/conhecimento")
@RequiredArgsConstructor
public class TenantKnowledgeWebController {

    private final KnowledgeTenantUseCase knowledgeTenantUseCase;
    private final KnowledgeArticleRepository articleRepository;
    private final TenantPortalWebService tenantPortalWebService;

    @GetMapping
    public String index(
            Authentication authentication,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String busca,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model
    ) {
        AppUserDetails currentUser = SecurityPrincipalUtils.getCurrentUser(authentication);
        Page<KnowledgeArticle> articles = knowledgeTenantUseCase.list(
                currentUser.getTenantId(), categoria, busca, page, size);

        model.addAttribute("tenantModules", tenantPortalWebService.listEnabledModules(authentication));
        model.addAttribute("activeMenu", "conhecimento");
        model.addAttribute("pageTitle", "Base de Conhecimento");
        model.addAttribute("pageSubtitle", "Guias e artigos técnicos");
        model.addAttribute("articles", articles.getContent());
        model.addAttribute("currentPage", articles.getNumber());
        model.addAttribute("totalPages", articles.getTotalPages());
        model.addAttribute("categorias", articleRepository.findDistinctCategoriaByPublicadoTrue());
        return "tenant/knowledge/index";
    }

    @GetMapping("/{id}")
    public String view(
            Authentication authentication,
            @PathVariable Long id,
            Model model
    ) {
        AppUserDetails currentUser = SecurityPrincipalUtils.getCurrentUser(authentication);
        KnowledgeArticle article = knowledgeTenantUseCase.getById(currentUser.getTenantId(), id);

        model.addAttribute("tenantModules", tenantPortalWebService.listEnabledModules(authentication));
        model.addAttribute("activeMenu", "conhecimento");
        model.addAttribute("pageTitle", article.getTitulo());
        model.addAttribute("pageSubtitle", article.getCategoria());
        model.addAttribute("article", article);
        return "tenant/knowledge/view";
    }
}
