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

    public boolean isInsufficientScope() {
        if (statusCode != 403 || responseBody == null) {
            return false;
        }
        return containsAny(
                "ACCESS_TOKEN_SCOPE_INSUFFICIENT",
                "insufficientPermissions",
                "Request had insufficient authentication scopes"
        );
    }

    public boolean isInvalidToken() {
        return statusCode == 401 || containsAny(
                "invalid_token",
                "Invalid Credentials",
                "authError",
                "UNAUTHENTICATED"
        );
    }

    public boolean isRateLimited() {
        return statusCode == 429 || containsAny(
                "rateLimitExceeded",
                "userRateLimitExceeded",
                "quotaExceeded",
                "RESOURCE_EXHAUSTED"
        );
    }

    public boolean isServiceDisabled() {
        return containsAny(
                "SERVICE_DISABLED",
                "accessNotConfigured",
                "Gmail API has not been used"
        );
    }

    public boolean isTemporaryFailure() {
        return statusCode == 408 || statusCode == 500 || statusCode == 502 || statusCode == 503 || statusCode == 504;
    }

    private boolean containsAny(String... fragments) {
        if (responseBody == null) {
            return false;
        }
        for (String fragment : fragments) {
            if (responseBody.contains(fragment)) {
                return true;
            }
        }
        return false;
    }
}
