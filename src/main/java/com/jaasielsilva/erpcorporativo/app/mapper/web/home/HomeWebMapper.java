package com.jaasielsilva.erpcorporativo.app.mapper.web.home;

import org.springframework.stereotype.Component;

import com.jaasielsilva.erpcorporativo.app.dto.web.home.HomeViewModel;

@Component
public class HomeWebMapper {

    public HomeViewModel toViewModel(String email) {
        return new HomeViewModel(email);
    }
}
