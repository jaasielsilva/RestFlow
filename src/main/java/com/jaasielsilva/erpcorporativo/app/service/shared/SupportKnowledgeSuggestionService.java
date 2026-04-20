package com.jaasielsilva.erpcorporativo.app.service.shared;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.jaasielsilva.erpcorporativo.app.dto.web.support.SupportKnowledgeSuggestionViewModel;
import com.jaasielsilva.erpcorporativo.app.model.KnowledgeArticle;
import com.jaasielsilva.erpcorporativo.app.repository.knowledge.KnowledgeArticleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SupportKnowledgeSuggestionService {

    private final KnowledgeArticleRepository knowledgeArticleRepository;

    public List<SupportKnowledgeSuggestionViewModel> suggest(Long tenantId, String categoria, String assunto) {
        List<SupportKnowledgeSuggestionViewModel> items = new ArrayList<>();
        String categoriaFilter = blankToNull(categoria);
        String assuntoFilter = blankToNull(assunto);

        List<KnowledgeArticle> byAssunto = knowledgeArticleRepository
                .findVisibleForTenant(tenantId, categoriaFilter, assuntoFilter, PageRequest.of(0, 5))
                .getContent();

        byAssunto.forEach(article -> items.add(toView(article)));

        if (items.size() < 5 && categoriaFilter != null) {
            List<KnowledgeArticle> byCategoria = knowledgeArticleRepository
                    .findVisibleForTenant(tenantId, categoriaFilter, null, PageRequest.of(0, 5))
                    .getContent();

            byCategoria.stream()
                    .filter(article -> items.stream().noneMatch(existing -> existing.id().equals(article.getId())))
                    .limit(5 - items.size())
                    .map(this::toView)
                    .forEach(items::add);
        }

        return items;
    }

    private SupportKnowledgeSuggestionViewModel toView(KnowledgeArticle article) {
        return new SupportKnowledgeSuggestionViewModel(article.getId(), article.getTitulo(), article.getCategoria());
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
