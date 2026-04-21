package com.jaasielsilva.erpcorporativo.app.dto.web.tenantadmin;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class TopServicoDTO {
    private String nome;
    private BigDecimal total;
}