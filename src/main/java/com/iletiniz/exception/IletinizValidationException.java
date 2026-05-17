package com.iletiniz.exception;

/** HTTP 400 / 422 — istek doğrulanamadı. */
public class IletinizValidationException extends IletinizApiException {

    public IletinizValidationException(String message, int status, String code, String body, String requestId) {
        super(message, status, code, body, requestId);
    }
}
