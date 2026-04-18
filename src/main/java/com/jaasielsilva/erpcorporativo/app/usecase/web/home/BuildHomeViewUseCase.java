package com.jaasielsilva.erpcorporativo.app.usecase.web.home;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.jaasielsilva.erpcorporativo.app.dto.web.home.HomeViewModel;
import com.jaasielsilva.erpcorporativo.app.mapper.web.home.HomeWebMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BuildHomeViewUseCase {

    private final HomeWebMapper homeWebMapper;

    public HomeViewModel execute(Authentication authentication) {
        return homeWebMapper.toViewModel(authentication.getName());
    }
}
