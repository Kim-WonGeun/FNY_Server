package com.mailservice.fny.analysis.infrastructure;

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
public class AgentWeeklyReportClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String weeklyAnalyzeUrl;

    public AgentWeeklyReportClient(
            ObjectMapper objectMapper,
            @Value("${fny.agent.base-url}") String baseUrl
    ) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = objectMapper;
        this.weeklyAnalyzeUrl = baseUrl.replaceAll("/$", "") + "/analyze-weekly";
    }

    public AgentWeeklyReportResponse analyzeWeekly(AgentWeeklyReportRequest request) {
        try {
            String body = objectMapper.writeValueAsString(request);
            HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(weeklyAnalyzeUrl))
                    .version(HttpClient.Version.HTTP_1_1)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new AgentAnalysisException(response.statusCode(), response.body());
            }

            return objectMapper.readValue(response.body(), AgentWeeklyReportResponse.class);
        } catch (JacksonException exception) {
            throw new IllegalStateException("Agent 주간 요약 JSON 처리에 실패했습니다.", exception);
        } catch (IOException exception) {
            throw new IllegalStateException("Agent 주간 요약 HTTP 호출에 실패했습니다.", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Agent 주간 요약 HTTP 호출이 중단되었습니다.", exception);
        }
    }
}
