package com.mailservice.fny.analysis.infrastructure;

public class AgentAnalysisException extends RuntimeException {

    private final int statusCode;
    private final String responseBody;

    public AgentAnalysisException(int statusCode, String responseBody) {
        super("Agent 분석 요청이 실패했습니다. status=" + statusCode);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }
}
