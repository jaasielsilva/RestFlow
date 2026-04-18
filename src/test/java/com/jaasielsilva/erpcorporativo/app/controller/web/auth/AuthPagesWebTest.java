package com.jaasielsilva.erpcorporativo.app.controller.web.auth;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AuthPagesWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldRenderLoginPage() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"loginForm\"")))
                .andExpect(content().string(containsString("name=\"email\"")))
                .andExpect(content().string(containsString("name=\"password\"")));
    }

    @Test
    void shouldAllowPasswordRecoveryPageWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/recuperar-senha"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Recuperar senha")));
    }

    @Test
    void shouldRedirectProtectedPageToLoginWhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/home"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", containsString("/login")));
    }
}

