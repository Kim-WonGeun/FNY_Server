package com.mailservice.fny.integration.gmail;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
public class GmailClient {

    private static final String GMAIL_API_BASE = "https://gmail.googleapis.com/gmail/v1/users/me";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public GmailClient(ObjectMapper objectMapper) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = objectMapper;
    }

    public GmailListResponse listMessages(String accessToken, int maxResults) {
        return listMessages(accessToken, maxResults, null);
    }

    public GmailListResponse listMessages(String accessToken, int maxResults, String pageToken) {
        return listMessages(accessToken, maxResults, pageToken, null);
    }

    public GmailListResponse listMessages(String accessToken, int maxResults, String pageToken, String query) {
        String url = GMAIL_API_BASE + "/messages?maxResults=" + maxResults;
        if (pageToken != null && !pageToken.isBlank()) {
            url += "&pageToken=" + URLEncoder.encode(pageToken, StandardCharsets.UTF_8);
        }
        if (query != null && !query.isBlank()) {
            url += "&q=" + URLEncoder.encode(query, StandardCharsets.UTF_8);
        }
        return get(accessToken, url, GmailListResponse.class);
    }

    public GmailMessageResponse getMessage(String accessToken, String messageId) {
        String encodedId = URLEncoder.encode(messageId, StandardCharsets.UTF_8);
        String url = GMAIL_API_BASE + "/messages/" + encodedId + "?format=full";
        return get(accessToken, url, GmailMessageResponse.class);
    }

    private <T> T get(String accessToken, String url, Class<T> responseType) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new GmailClientException(response.statusCode(), response.body());
            }
            return objectMapper.readValue(response.body(), responseType);
        } catch (JacksonException exception) {
            throw new IllegalStateException("Gmail API 응답 JSON 처리에 실패했습니다.", exception);
        } catch (IOException exception) {
            throw new IllegalStateException("Gmail API 호출에 실패했습니다.", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Gmail API 호출이 중단되었습니다.", exception);
        }
    }
}
