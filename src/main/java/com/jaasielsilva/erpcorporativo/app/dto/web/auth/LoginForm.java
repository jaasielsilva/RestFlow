package com.jaasielsilva.erpcorporativo.app.dto.web.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginForm {
    private String email;
    private String password;
    private boolean remember;
}
