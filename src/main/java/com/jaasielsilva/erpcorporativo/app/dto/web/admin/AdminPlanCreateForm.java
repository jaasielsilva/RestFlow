package com.jaasielsilva.erpcorporativo.app.dto.web.admin;

import java.util.HashSet;
import java.util.Set;

import com.jaasielsilva.erpcorporativo.app.model.PlanTier;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminPlanCreateForm {

    @NotBlank(message = "Código é obrigatório")
    @Size(max = 60)
    private String codigo;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 120)
    private String nome;

    @Size(max = 255)
    private String descricao;

    private boolean ativo = true;

    @NotNull(message = "Tier é obrigatório")
    private PlanTier tier = PlanTier.START;

    private Integer maxUsers;

    private Integer maxStorageGb;

    private boolean annualDiscountEligible;

    @Size(max = 30)
    private String onboardingTemplate;

    private Set<Long> moduleIds = new HashSet<>();

    private Set<Long> addonIds = new HashSet<>();
}
