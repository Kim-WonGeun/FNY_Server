package com.mailservice.fny.integration.gmail;

public class GmailClientException extends RuntimeException {

    private final int statusCode;
    private final String responseBody;

    public GmailClientException(int statusCode, String responseBody) {
        super("Gmail API 요청 실패. status=" + statusCode);
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
