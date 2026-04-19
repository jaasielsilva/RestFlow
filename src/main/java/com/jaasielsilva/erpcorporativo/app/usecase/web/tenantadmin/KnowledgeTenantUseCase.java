package com.jaasielsilva.erpcorporativo.app.usecase.web.tenantadmin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.jaasielsilva.erpcorporativo.app.exception.ResourceNotFoundException;
import com.jaasielsilva.erpcorporativo.app.model.ArticleVisibility;
import com.jaasielsilva.erpcorporativo.app.model.KnowledgeArticle;
import com.jaasielsilva.erpcorporativo.app.repository.knowledge.KnowledgeArticleRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class KnowledgeTenantUseCase {

    private final KnowledgeArticleRepository articleRepository;

    @Transactional(readOnly = true)
    public Page<KnowledgeArticle> list(Long tenantId, String categoria, String busca, int page, int size) {
        Pageable pageable = PageRequest.of(page, normalizeSize(size));
        return articleRepository.findVisibleForTenant(
                tenantId,
                blankToNull(categoria),
                blankToNull(busca),
                pageable
        );
    }

    @Transactional(readOnly = true)
    public KnowledgeArticle getById(Long tenantId, Long id) {
        KnowledgeArticle article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Artigo não encontrado: " + id));

        if (!article.isPublicado()) {
            throw new ResourceNotFoundException("Artigo não disponível.");
        }

        boolean visible = article.getVisibilidade() == ArticleVisibility.PUBLICO
                || (article.getVisibilidade() == ArticleVisibility.PRIVADO
                    && article.getTenant() != null
                    && article.getTenant().getId().equals(tenantId));

        if (!visible) {
            throw new ResourceNotFoundException("Artigo não disponível para este tenant.");
        }

        return article;
    }

    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    private int normalizeSize(int size) {
        return (size <= 0) ? 20 : Math.min(size, 100);
    }
}
