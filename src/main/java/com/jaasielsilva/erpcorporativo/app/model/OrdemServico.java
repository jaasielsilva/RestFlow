package com.jaasielsilva.erpcorporativo.app.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "ordens_servico")
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdemServico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Número sequencial legível: OS-0001, OS-0002... */
    @Column(nullable = false, length = 20)
    private String numero;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    /** Nome do cliente/empresa solicitante */
    @Column(nullable = false, length = 150)
    private String clienteNome;

    @Column(length = 150)
    private String clienteEmail;

    @Column(length = 30)
    private String clienteTelefone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrdemServicoStatus status;

    /** Valor estimado/cobrado pelo serviço */
    @Column(precision = 12, scale = 2)
    private BigDecimal valor;

    /** Data prevista de conclusão */
    @Column(name = "data_prevista")
    private LocalDate dataPrevista;

    /** Usuário responsável pela OS dentro do tenant */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsavel_id", foreignKey = @ForeignKey(name = "fk_os_responsavel"))
    private Usuario responsavel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false, foreignKey = @ForeignKey(name = "fk_os_tenant"))
    private Tenant tenant;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
