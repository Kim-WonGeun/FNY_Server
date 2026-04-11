package com.mailservice.fny.auth.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void providersReturnsSupportedOAuthProviders() throws Exception {
        mockMvc.perform(get("/api/auth/providers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].provider", is("GOOGLE")))
                .andExpect(jsonPath("$[1].provider", is("MICROSOFT")));
    }

    @Test
    void provisionAccountCreatesUserAndMailAccount() throws Exception {
        mockMvc.perform(post("/api/auth/oauth/provision")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "provider": "google",
                                  "providerAccountId": "google_new_user",
                                  "accountEmail": "new-user@test.com",
                                  "accountName": "신규 사용자",
                                  "accessToken": "access-token",
                                  "refreshToken": "refresh-token"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.mailAccountId").exists())
                .andExpect(jsonPath("$.provider", is("GOOGLE")))
                .andExpect(jsonPath("$.accountEmail", is("new-user@test.com")));
    }
}
