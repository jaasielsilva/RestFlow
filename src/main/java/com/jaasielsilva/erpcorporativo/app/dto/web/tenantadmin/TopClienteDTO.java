// TopClienteDTO.java
package com.jaasielsilva.erpcorporativo.app.dto.web.tenantadmin;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class TopClienteDTO {
    private String nome;
    private BigDecimal total;
}