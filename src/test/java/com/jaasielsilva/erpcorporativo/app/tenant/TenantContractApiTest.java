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

import com.jaasielsilva.erpcorporativo.app.model.Tenant;
import com.jaasielsilva.erpcorporativo.app.repository.tenant.TenantRepository;

@SpringBootTest
@AutoConfigureMockMvc
class TenantContractApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TenantRepository tenantRepository;

    @Test
    void shouldReturn400WhenTenantIsMissingOnTenantAdminRoutes() throws Exception {
        mockMvc.perform(get("/api/v1/tenant-admin/users"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().string(containsString("\"code\":\"BAD_REQUEST\"")))
                .andExpect(content().string(containsString("Tenant obrigatório")));
    }

    @Test
    void shouldReturn400WhenTenantIsMalformedOnTenantAdminRoutes() throws Exception {
        mockMvc.perform(get("/api/v1/tenant-admin/users").header("X-Tenant-Id", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().string(containsString("\"code\":\"BAD_REQUEST\"")))
                .andExpect(content().string(containsString("tenantId inválido")));
    }

    @Test
    void shouldReturn401WhenTenantIsInactive() throws Exception {
        Tenant tenant = tenantRepository.save(Tenant.builder()
                .nome("Empresa Inativa")
                .slug("empresa-inativa")
                .ativo(false)
                .build());

        mockMvc.perform(get("/api/v1/tenant-admin/users").header("X-Tenant-Id", tenant.getId().toString()))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().string(containsString("\"code\":\"UNAUTHORIZED\"")))
                .andExpect(content().string(containsString("Tenant inexistente ou inativo")));
    }

    @Test
    void shouldProceedToAuthFlowWhenTenantIsValidButUnauthenticated() throws Exception {
        Tenant tenant = tenantRepository.save(Tenant.builder()
                .nome("Empresa Ativa")
                .slug("empresa-ativa")
                .ativo(true)
                .build());

        mockMvc.perform(get("/api/v1/tenant-admin/users").header("X-Tenant-Id", tenant.getId().toString()))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().string(containsString("\"code\":\"UNAUTHORIZED\"")))
                .andExpect(content().string(containsString("Autenticação obrigatória")));
    }
}

