package com.iletiniz.exception;

/** HTTP 429 — istek hız limitini aştı. */
public class IletinizRateLimitException extends IletinizApiException {

    public IletinizRateLimitException(String message, int status, String code, String body, String requestId) {
        super(message, status, code, body, requestId);
    }
}
