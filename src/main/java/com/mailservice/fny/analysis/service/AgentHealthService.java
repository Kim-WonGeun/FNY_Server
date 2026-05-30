package com.mailservice.fny.analysis.service;

import com.mailservice.fny.analysis.dto.AgentHealthResponse;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AgentHealthService {

    private final HttpClient httpClient;
    private final boolean enabled;
    private final String baseUrl;
    private final String healthUrl;

    public AgentHealthService(
            @Value("${fny.agent.enabled:false}") boolean enabled,
            @Value("${fny.agent.base-url}") String baseUrl
    ) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
        this.enabled = enabled;
        this.baseUrl = baseUrl.replaceAll("/$", "");
        this.healthUrl = this.baseUrl + "/health";
    }

    public AgentHealthResponse check() {
        if (!enabled) {
            return new AgentHealthResponse(
                    false,
                    false,
                    "DISABLED",
                    baseUrl,
                    "Agent 분석 기능이 비활성화되어 있습니다.",
                    LocalDateTime.now()
            );
        }

        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(healthUrl))
                    .timeout(Duration.ofSeconds(3))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return new AgentHealthResponse(
                        true,
                        true,
                        "CONNECTED",
                        baseUrl,
                        "Agent 서버가 응답 중입니다.",
                        LocalDateTime.now()
                );
            }

            return new AgentHealthResponse(
                    true,
                    false,
                    "UNREACHABLE",
                    baseUrl,
                    "Agent health 응답이 정상이 아닙니다. status=" + response.statusCode(),
                    LocalDateTime.now()
            );
        } catch (IOException exception) {
            String detail = exception.getMessage();
            return unreachable(detail == null || detail.isBlank()
                    ? "Agent 서버에 연결할 수 없습니다."
                    : "Agent 서버에 연결할 수 없습니다. " + detail);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return unreachable("Agent health 확인이 중단되었습니다.");
        } catch (RuntimeException exception) {
            return unreachable("Agent health 확인에 실패했습니다. " + exception.getMessage());
        }
    }

    private AgentHealthResponse unreachable(String message) {
        return new AgentHealthResponse(
                true,
                false,
                "UNREACHABLE",
                baseUrl,
                message,
                LocalDateTime.now()
        );
    }
}
