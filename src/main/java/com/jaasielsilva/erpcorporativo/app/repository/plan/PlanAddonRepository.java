package com.jaasielsilva.erpcorporativo.app.repository.plan;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jaasielsilva.erpcorporativo.app.model.PlanAddon;

public interface PlanAddonRepository extends JpaRepository<PlanAddon, Long> {

    Optional<PlanAddon> findByCodigoIgnoreCase(String codigo);

    List<PlanAddon> findAllByAtivoTrueOrderByNomeAsc();
}
