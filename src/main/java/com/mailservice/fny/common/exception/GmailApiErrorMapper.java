package com.mailservice.fny.common.exception;

import com.mailservice.fny.integration.gmail.GmailClientException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class GmailApiErrorMapper {

    MappedError map(GmailClientException exception) {
        if (exception.isInsufficientScope()) {
            return new MappedError(
                    HttpStatus.FORBIDDEN,
                    "GMAIL_SCOPE_INSUFFICIENT",
                    "Gmail 읽기 권한이 부족합니다. Google 계정의 FNY-Service 앱 권한을 삭제한 뒤 다시 로그인해 주세요."
            );
        }
        if (exception.isInvalidToken()) {
            return new MappedError(
                    HttpStatus.UNAUTHORIZED,
                    "GMAIL_TOKEN_INVALID",
                    "Google 로그인 토큰이 만료되었거나 유효하지 않습니다. 다시 로그인해 주세요."
            );
        }
        if (exception.isServiceDisabled()) {
            return new MappedError(
                    HttpStatus.FORBIDDEN,
                    "GMAIL_SERVICE_DISABLED",
                    "Google Cloud 프로젝트에서 Gmail API가 비활성화되어 있습니다. Gmail API를 사용 설정한 뒤 다시 시도해 주세요."
            );
        }
        if (exception.isRateLimited()) {
            return new MappedError(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "GMAIL_RATE_LIMITED",
                    "Gmail API 요청 한도에 도달했습니다. 잠시 후 다시 시도해 주세요."
            );
        }
        if (exception.isTemporaryFailure()) {
            return new MappedError(
                    HttpStatus.BAD_GATEWAY,
                    "GMAIL_TEMPORARY_FAILURE",
                    "Gmail API가 일시적으로 응답하지 않습니다. 잠시 후 다시 시도해 주세요."
            );
        }

        return new MappedError(
                HttpStatus.BAD_GATEWAY,
                "GMAIL_API_FAILED",
                "Gmail API 요청에 실패했습니다. 잠시 후 다시 시도해 주세요."
        );
    }

    record MappedError(HttpStatus status, String code, String message) {
    }
}
