package com.mailservice.fny.analysis.infrastructure;

import com.mailservice.fny.mailbox.entity.EmailMessage;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
public class AgentAnalysisClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String analyzeUrl;

    public AgentAnalysisClient(
            ObjectMapper objectMapper,
            @Value("${fny.agent.base-url}") String baseUrl
    ) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = objectMapper;
        this.analyzeUrl = baseUrl.replaceAll("/$", "") + "/analyze";
    }

    public AgentAnalysisResponse analyze(EmailMessage email, AgentPromptTemplateRequest prompt) {
        try {
            String body = objectMapper.writeValueAsString(AgentAnalysisRequest.from(email, prompt));
            HttpRequest request = HttpRequest.newBuilder(URI.create(analyzeUrl))
                    .version(HttpClient.Version.HTTP_1_1)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new AgentAnalysisException(response.statusCode(), response.body());
            }

            return objectMapper.readValue(response.body(), AgentAnalysisResponse.class);
        } catch (JacksonException exception) {
            throw new IllegalStateException("Agent 요청/응답 JSON 처리에 실패했습니다.", exception);
        } catch (IOException exception) {
            throw new IllegalStateException("Agent HTTP 호출에 실패했습니다.", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Agent HTTP 호출이 중단되었습니다.", exception);
        }
    }
}
