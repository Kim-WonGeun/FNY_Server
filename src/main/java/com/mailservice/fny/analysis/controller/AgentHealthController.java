package com.mailservice.fny.analysis.controller;

import com.mailservice.fny.analysis.dto.AgentHealthResponse;
import com.mailservice.fny.analysis.service.AgentHealthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agent")
public class AgentHealthController {

    private final AgentHealthService agentHealthService;

    public AgentHealthController(AgentHealthService agentHealthService) {
        this.agentHealthService = agentHealthService;
    }

    @GetMapping("/health")
    public AgentHealthResponse health() {
        return agentHealthService.check();
    }
}
