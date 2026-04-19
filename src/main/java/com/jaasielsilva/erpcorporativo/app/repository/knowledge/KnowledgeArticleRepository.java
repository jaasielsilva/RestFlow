package com.jaasielsilva.erpcorporativo.app.repository.knowledge;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.jaasielsilva.erpcorporativo.app.model.KnowledgeArticle;

public interface KnowledgeArticleRepository extends JpaRepository<KnowledgeArticle, Long> {

    /** Todos os artigos (SUPER_ADMIN) com filtro opcional de categoria e busca por título */
    @Query("""
            select a from KnowledgeArticle a
            where (:categoria is null or lower(a.categoria) = lower(:categoria))
              and (:busca is null or lower(a.titulo) like lower(concat('%', :busca, '%')))
            order by a.createdAt desc
            """)
    Page<KnowledgeArticle> findAllAdmin(
            @Param("categoria") String categoria,
            @Param("busca") String busca,
            Pageable pageable
    );

    /**
     * Artigos visíveis para um tenant:
     * - PUBLICO + publicado
     * - PRIVADO + publicado + tenant_id = tenantId
     */
    @Query("""
            select a from KnowledgeArticle a
            where a.publicado = true
              and (
                    a.visibilidade = 'PUBLICO'
                    or (a.visibilidade = 'PRIVADO' and a.tenant.id = :tenantId)
                  )
              and (:categoria is null or lower(a.categoria) = lower(:categoria))
              and (:busca is null or lower(a.titulo) like lower(concat('%', :busca, '%')))
            order by a.createdAt desc
            """)
    Page<KnowledgeArticle> findVisibleForTenant(
            @Param("tenantId") Long tenantId,
            @Param("categoria") String categoria,
            @Param("busca") String busca,
            Pageable pageable
    );

    List<String> findDistinctCategoriaByPublicadoTrue();

    @Query("select distinct a.categoria from KnowledgeArticle a order by a.categoria")
    List<String> findAllCategorias();
}
