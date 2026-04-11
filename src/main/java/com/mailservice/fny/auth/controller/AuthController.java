package com.mailservice.fny.auth.controller;

import com.mailservice.fny.auth.dto.AuthProviderResponse;
import com.mailservice.fny.auth.dto.AuthSessionResponse;
import com.mailservice.fny.auth.dto.OAuthProvisionRequest;
import com.mailservice.fny.auth.service.OAuthAccountService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final OAuthAccountService oAuthAccountService;

    public AuthController(OAuthAccountService oAuthAccountService) {
        this.oAuthAccountService = oAuthAccountService;
    }

    @GetMapping("/providers")
    public List<AuthProviderResponse> getProviders() {
        return List.of(
                new AuthProviderResponse("GOOGLE", "/oauth2/authorization/google", "/api/auth/oauth/provision"),
                new AuthProviderResponse("MICROSOFT", "/oauth2/authorization/microsoft", "/api/auth/oauth/provision")
        );
    }

    @PostMapping("/oauth/provision")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthSessionResponse provisionAccount(@Valid @RequestBody OAuthProvisionRequest request) {
        return oAuthAccountService.provisionAccount(request);
    }
}
