package com.jaasielsilva.erpcorporativo.app.repository.module;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jaasielsilva.erpcorporativo.app.model.PlatformModule;

public interface PlatformModuleRepository extends JpaRepository<PlatformModule, Long> {

    Optional<PlatformModule> findByCodigoIgnoreCase(String codigo);
}
