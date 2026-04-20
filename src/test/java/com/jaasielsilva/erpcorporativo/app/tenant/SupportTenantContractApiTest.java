package com.jaasielsilva.erpcorporativo.app.tenant;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class SupportTenantContractApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldRequireTenantHeaderOnSupportRoutes() throws Exception {
        mockMvc.perform(get("/api/v1/tenant-admin/support/tickets"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().string(containsString("\"code\":\"BAD_REQUEST\"")))
                .andExpect(content().string(containsString("Tenant obrigatório")));
    }

    @Test
    void shouldRejectMalformedTenantHeaderOnSupportRoutes() throws Exception {
        mockMvc.perform(get("/api/v1/tenant-admin/support/tickets").header("X-Tenant-Id", "invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().string(containsString("\"code\":\"BAD_REQUEST\"")))
                .andExpect(content().string(containsString("tenantId inválido")));
    }
}
