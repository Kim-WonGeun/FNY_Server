package com.mailservice.fny.auth.service;

import com.mailservice.fny.auth.dto.AuthSessionResponse;
import com.mailservice.fny.auth.dto.OAuthProvisionRequest;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final OAuthAccountService oAuthAccountService;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final String frontendBaseUrl;

    public OAuth2LoginSuccessHandler(
            OAuthAccountService oAuthAccountService,
            OAuth2AuthorizedClientService authorizedClientService,
            @Value("${fny.frontend.base-url:http://localhost:5173}") String frontendBaseUrl
    ) {
        this.oAuthAccountService = oAuthAccountService;
        this.authorizedClientService = authorizedClientService;
        this.frontendBaseUrl = frontendBaseUrl;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        if (!(authentication instanceof OAuth2AuthenticationToken token)) {
            response.sendRedirect(frontendBaseUrl);
            return;
        }

        Map<String, Object> attributes = token.getPrincipal().getAttributes();
        String provider = token.getAuthorizedClientRegistrationId();
        String providerAccountId = asString(attributes.get("sub"));
        String accountEmail = asString(attributes.get("email"));
        String accountName = asString(attributes.getOrDefault("name", accountEmail));

        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                token.getAuthorizedClientRegistrationId(),
                token.getName()
        );

        OAuth2AccessToken accessToken = client != null ? client.getAccessToken() : null;
        OAuth2RefreshToken refreshToken = client != null ? client.getRefreshToken() : null;

        AuthSessionResponse session = oAuthAccountService.provisionAccount(new OAuthProvisionRequest(
                provider,
                providerAccountId,
                accountEmail,
                accountName,
                accessToken != null ? accessToken.getTokenValue() : null,
                refreshToken != null ? refreshToken.getTokenValue() : null,
                accessToken != null && accessToken.getExpiresAt() != null
                        ? LocalDateTime.ofInstant(accessToken.getExpiresAt(), ZoneId.systemDefault())
                        : null
        ));

        response.sendRedirect(frontendBaseUrl.replaceAll("/$", "") + "/auth/callback"
                + "?userId=" + encode(session.userId())
                + "&displayName=" + encode(session.displayName())
                + "&primaryEmail=" + encode(session.primaryEmail())
                + "&mailAccountId=" + encode(session.mailAccountId())
                + "&provider=" + encode(session.provider())
                + "&accountEmail=" + encode(session.accountEmail()));
    }

    private static String asString(Object value) {
        return value == null ? "" : value.toString();
    }

    private static String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }
}
