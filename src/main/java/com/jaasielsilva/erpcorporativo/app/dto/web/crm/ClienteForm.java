package com.jaasielsilva.erpcorporativo.app.dto.web.crm;

import com.jaasielsilva.erpcorporativo.app.model.Genero;
import com.jaasielsilva.erpcorporativo.app.model.StatusCliente;
import com.jaasielsilva.erpcorporativo.app.model.TipoCliente;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class ClienteForm {

    @NotNull(message = "Tipo é obrigatório")
    private TipoCliente tipo;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 200)
    private String nome;

    @Size(max = 20)
    private String documento;

    @Size(max = 150)
    private String email;

    @Size(max = 30)
    private String telefonePrincipal;

    @Size(max = 30)
    private String telefoneSecundario;

    @Size(max = 200)
    private String logradouro;

    @Size(max = 20)
    private String numeroEndereco;

    @Size(max = 100)
    private String complemento;

    @Size(max = 100)
    private String bairro;

    @Size(max = 100)
    private String cidade;

    @Size(max = 2)
    private String estado;

    @Size(max = 10)
    private String cep;

    @NotNull(message = "Status é obrigatório")
    private StatusCliente status = StatusCliente.PROSPECTO;

    private String observacoes;

    // Pessoa Física
    private LocalDate dataNascimento;
    private Genero genero;

    // Pessoa Jurídica
    @Size(max = 200)
    private String nomeFantasia;

    @Size(max = 30)
    private String inscricaoEstadual;

    @Size(max = 150)
    private String contatoPrincipal;

    @Size(max = 500)
    private String motivoBloqueio;
}
