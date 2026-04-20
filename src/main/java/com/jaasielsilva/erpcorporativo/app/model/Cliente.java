package com.jaasielsilva.erpcorporativo.app.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "clientes",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_clientes_tenant_documento", columnNames = {"tenant_id", "documento"})
        }
)
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Número sequencial legível: CLI-0001, CLI-0002... */
    @Column(nullable = false, length = 20)
    private String numero;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoCliente tipo;

    /** Nome completo (PF) ou Razão Social (PJ) */
    @Column(nullable = false, length = 200)
    private String nome;

    /** CPF (PF) ou CNPJ (PJ) — opcional, único por tenant */
    @Column(length = 20)
    private String documento;

    @Column(length = 150)
    private String email;

    @Column(name = "telefone_principal", length = 30)
    private String telefonePrincipal;

    @Column(name = "telefone_secundario", length = 30)
    private String telefoneSecundario;

    // Endereço
    @Column(length = 200)
    private String logradouro;

    @Column(length = 20)
    private String numero_endereco;

    @Column(length = 100)
    private String complemento;

    @Column(length = 100)
    private String bairro;

    @Column(length = 100)
    private String cidade;

    @Column(length = 2)
    private String estado;

    @Column(length = 10)
    private String cep;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatusCliente status = StatusCliente.PROSPECTO;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    // Campos específicos de Pessoa Física
    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Genero genero;

    // Campos específicos de Pessoa Jurídica
    @Column(name = "nome_fantasia", length = 200)
    private String nomeFantasia;

    @Column(name = "inscricao_estadual", length = 30)
    private String inscricaoEstadual;

    @Column(name = "contato_principal", length = 150)
    private String contatoPrincipal;

    /** Motivo do bloqueio — obrigatório quando status = BLOQUEADO */
    @Column(name = "motivo_bloqueio", length = 500)
    private String motivoBloqueio;

    /** Data/hora da última interação registrada */
    @Column(name = "ultima_interacao_em")
    private LocalDateTime ultimaInteracaoEm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false, foreignKey = @ForeignKey(name = "fk_clientes_tenant"))
    private Tenant tenant;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Interacao> interacoes = new ArrayList<>();

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Oportunidade> oportunidades = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
