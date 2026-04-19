package com.jaasielsilva.erpcorporativo.app.usecase.web.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.jaasielsilva.erpcorporativo.app.dto.web.admin.KnowledgeArticleForm;
import com.jaasielsilva.erpcorporativo.app.exception.ResourceNotFoundException;
import com.jaasielsilva.erpcorporativo.app.model.ArticleVisibility;
import com.jaasielsilva.erpcorporativo.app.model.KnowledgeArticle;
import com.jaasielsilva.erpcorporativo.app.model.Tenant;
import com.jaasielsilva.erpcorporativo.app.repository.knowledge.KnowledgeArticleRepository;
import com.jaasielsilva.erpcorporativo.app.repository.tenant.TenantRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class KnowledgeAdminUseCase {

    private final KnowledgeArticleRepository articleRepository;
    private final TenantRepository tenantRepository;

    @Transactional(readOnly = true)
    public Page<KnowledgeArticle> list(String categoria, String busca, int page, int size) {
        Pageable pageable = PageRequest.of(page, normalizeSize(size));
        return articleRepository.findAllAdmin(
                blankToNull(categoria),
                blankToNull(busca),
                pageable
        );
    }

    @Transactional(readOnly = true)
    public KnowledgeArticle getById(Long id) {
        return findArticle(id);
    }

    @Transactional
    public KnowledgeArticle create(KnowledgeArticleForm form) {
        Tenant tenant = resolveTenant(form);

        KnowledgeArticle article = KnowledgeArticle.builder()
                .titulo(form.getTitulo().trim())
                .resumo(form.getResumo() != null ? form.getResumo().trim() : null)
                .conteudo(form.getConteudo())
                .categoria(form.getCategoria().trim())
                .visibilidade(form.getVisibilidade())
                .publicado(form.isPublicado())
                .tenant(tenant)
                .build();

        return articleRepository.save(article);
    }

    @Transactional
    public KnowledgeArticle update(Long id, KnowledgeArticleForm form) {
        KnowledgeArticle article = findArticle(id);
        Tenant tenant = resolveTenant(form);

        article.setTitulo(form.getTitulo().trim());
        article.setResumo(form.getResumo() != null ? form.getResumo().trim() : null);
        article.setConteudo(form.getConteudo());
        article.setCategoria(form.getCategoria().trim());
        article.setVisibilidade(form.getVisibilidade());
        article.setPublicado(form.isPublicado());
        article.setTenant(tenant);

        return articleRepository.save(article);
    }

    @Transactional
    public void delete(Long id) {
        articleRepository.delete(findArticle(id));
    }

    @Transactional
    public void togglePublicado(Long id) {
        KnowledgeArticle article = findArticle(id);
        article.setPublicado(!article.isPublicado());
        articleRepository.save(article);
    }

    private KnowledgeArticle findArticle(Long id) {
        return articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Artigo não encontrado: " + id));
    }

    private Tenant resolveTenant(KnowledgeArticleForm form) {
        if (form.getVisibilidade() == ArticleVisibility.PRIVADO && form.getTenantId() != null) {
            return tenantRepository.findById(form.getTenantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Tenant não encontrado: " + form.getTenantId()));
        }
        return null;
    }

    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    private int normalizeSize(int size) {
        return (size <= 0) ? 20 : Math.min(size, 100);
    }
}
