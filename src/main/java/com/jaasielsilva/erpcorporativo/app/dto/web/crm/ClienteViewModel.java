package com.jaasielsilva.erpcorporativo.app.dto.web.crm;

import com.jaasielsilva.erpcorporativo.app.model.Genero;
import com.jaasielsilva.erpcorporativo.app.model.StatusCliente;
import com.jaasielsilva.erpcorporativo.app.model.TipoCliente;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ClienteViewModel(
        Long id,
        String numero,
        TipoCliente tipo,
        String nome,
        String documento,
        String email,
        String telefonePrincipal,
        String telefoneSecundario,
        String logradouro,
        String numeroEndereco,
        String complemento,
        String bairro,
        String cidade,
        String estado,
        String cep,
        StatusCliente status,
        String observacoes,
        LocalDate dataNascimento,
        Genero genero,
        String nomeFantasia,
        String inscricaoEstadual,
        String contatoPrincipal,
        String motivoBloqueio,
        LocalDateTime ultimaInteracaoEm,
        long oportunidadesAbertas,
        List<InteracaoViewModel> interacoes,
        List<OportunidadeViewModel> oportunidades,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
