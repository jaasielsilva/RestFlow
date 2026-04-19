package com.jaasielsilva.erpcorporativo.app.repository.settings;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jaasielsilva.erpcorporativo.app.model.PlatformSetting;

public interface PlatformSettingRepository extends JpaRepository<PlatformSetting, String> {

    Optional<PlatformSetting> findByChave(String chave);
}
