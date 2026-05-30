package com.mailservice.fny.config;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "fny.agent.enabled=false")
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void apiRequiresAuthenticationByDefault() throws Exception {
        mockMvc.perform(get("/api/me/overview"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code", is("AUTHENTICATION_REQUIRED")));
    }

    @Test
    void authProviderEndpointIsPublic() throws Exception {
        mockMvc.perform(get("/api/auth/providers"))
                .andExpect(status().isOk());
    }

    @Test
    void agentHealthEndpointIsPublic() throws Exception {
        mockMvc.perform(get("/api/agent/health"))
                .andExpect(status().isOk());
    }
}
