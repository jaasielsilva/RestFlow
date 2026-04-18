package com.jaasielsilva.erpcorporativo.app.service.web.home;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.jaasielsilva.erpcorporativo.app.dto.web.home.HomeViewModel;
import com.jaasielsilva.erpcorporativo.app.usecase.web.home.BuildHomeViewUseCase;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HomeWebService {

    private final BuildHomeViewUseCase buildHomeViewUseCase;

    public HomeViewModel buildHomeView(Authentication authentication) {
        return buildHomeViewUseCase.execute(authentication);
    }
}
