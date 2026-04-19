package com.jaasielsilva.erpcorporativo.app.controller.web.admin;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.jaasielsilva.erpcorporativo.app.dto.web.admin.KnowledgeArticleForm;
import com.jaasielsilva.erpcorporativo.app.exception.AppException;
import com.jaasielsilva.erpcorporativo.app.model.ArticleVisibility;
import com.jaasielsilva.erpcorporativo.app.model.KnowledgeArticle;
import com.jaasielsilva.erpcorporativo.app.repository.knowledge.KnowledgeArticleRepository;
import com.jaasielsilva.erpcorporativo.app.repository.tenant.TenantRepository;
import com.jaasielsilva.erpcorporativo.app.usecase.web.admin.KnowledgeAdminUseCase;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/conhecimento")
@RequiredArgsConstructor
public class AdminKnowledgeWebController {

    private final KnowledgeAdminUseCase knowledgeAdminUseCase;
    private final KnowledgeArticleRepository articleRepository;
    private final TenantRepository tenantRepository;

    @GetMapping
    public String index(
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String busca,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model
    ) {
        Page<KnowledgeArticle> articles = knowledgeAdminUseCase.list(categoria, busca, page, size);
        model.addAttribute("articles", articles.getContent());
        model.addAttribute("currentPage", articles.getNumber());
        model.addAttribute("totalPages", articles.getTotalPages());
        model.addAttribute("categorias", articleRepository.findAllCategorias());
        model.addAttribute("activeMenu", "knowledge");
        model.addAttribute("pageTitle", "Base de Conhecimento");
        model.addAttribute("pageSubtitle", "Guias e artigos técnicos da plataforma");
        return "admin/knowledge/index";
    }

    @GetMapping("/new")
    public String newArticle(Model model) {
        model.addAttribute("form", new KnowledgeArticleForm());
        model.addAttribute("tenants", tenantRepository.findAll());
        model.addAttribute("visibilidades", ArticleVisibility.values());
        model.addAttribute("categorias", articleRepository.findAllCategorias());
        model.addAttribute("activeMenu", "knowledge");
        model.addAttribute("pageTitle", "Base de Conhecimento");
        model.addAttribute("pageSubtitle", "Novo artigo");
        model.addAttribute("isEdit", false);
        return "admin/knowledge/form";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("form") KnowledgeArticleForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("tenants", tenantRepository.findAll());
            model.addAttribute("visibilidades", ArticleVisibility.values());
            model.addAttribute("categorias", articleRepository.findAllCategorias());
            model.addAttribute("activeMenu", "knowledge");
            model.addAttribute("pageTitle", "Base de Conhecimento");
            model.addAttribute("pageSubtitle", "Novo artigo");
            model.addAttribute("isEdit", false);
            return "admin/knowledge/form";
        }

        try {
            knowledgeAdminUseCase.create(form);
            redirectAttributes.addFlashAttribute("toastSuccess", "Artigo criado com sucesso.");
            return "redirect:/admin/conhecimento";
        } catch (AppException ex) {
            redirectAttributes.addFlashAttribute("toastError", ex.getMessage());
            return "redirect:/admin/conhecimento";
        }
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        KnowledgeArticle article = knowledgeAdminUseCase.getById(id);

        KnowledgeArticleForm form = new KnowledgeArticleForm();
        form.setTitulo(article.getTitulo());
        form.setResumo(article.getResumo());
        form.setConteudo(article.getConteudo());
        form.setCategoria(article.getCategoria());
        form.setVisibilidade(article.getVisibilidade());
        form.setPublicado(article.isPublicado());
        form.setTenantId(article.getTenant() != null ? article.getTenant().getId() : null);

        model.addAttribute("form", form);
        model.addAttribute("articleId", id);
        model.addAttribute("tenants", tenantRepository.findAll());
        model.addAttribute("visibilidades", ArticleVisibility.values());
        model.addAttribute("categorias", articleRepository.findAllCategorias());
        model.addAttribute("activeMenu", "knowledge");
        model.addAttribute("pageTitle", "Base de Conhecimento");
        model.addAttribute("pageSubtitle", "Editar artigo");
        model.addAttribute("isEdit", true);
        return "admin/knowledge/form";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("form") KnowledgeArticleForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("articleId", id);
            model.addAttribute("tenants", tenantRepository.findAll());
            model.addAttribute("visibilidades", ArticleVisibility.values());
            model.addAttribute("categorias", articleRepository.findAllCategorias());
            model.addAttribute("activeMenu", "knowledge");
            model.addAttribute("pageTitle", "Base de Conhecimento");
            model.addAttribute("pageSubtitle", "Editar artigo");
            model.addAttribute("isEdit", true);
            return "admin/knowledge/form";
        }

        try {
            knowledgeAdminUseCase.update(id, form);
            redirectAttributes.addFlashAttribute("toastSuccess", "Artigo atualizado com sucesso.");
        } catch (AppException ex) {
            redirectAttributes.addFlashAttribute("toastError", ex.getMessage());
        }

        return "redirect:/admin/conhecimento";
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            knowledgeAdminUseCase.togglePublicado(id);
            redirectAttributes.addFlashAttribute("toastSuccess", "Status do artigo atualizado.");
        } catch (AppException ex) {
            redirectAttributes.addFlashAttribute("toastError", ex.getMessage());
        }
        return "redirect:/admin/conhecimento";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            knowledgeAdminUseCase.delete(id);
            redirectAttributes.addFlashAttribute("toastSuccess", "Artigo removido.");
        } catch (AppException ex) {
            redirectAttributes.addFlashAttribute("toastError", ex.getMessage());
        }
        return "redirect:/admin/conhecimento";
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        KnowledgeArticle article = knowledgeAdminUseCase.getById(id);
        model.addAttribute("article", article);
        model.addAttribute("activeMenu", "knowledge");
        model.addAttribute("pageTitle", "Base de Conhecimento");
        model.addAttribute("pageSubtitle", article.getTitulo());
        return "admin/knowledge/view";
    }
}
