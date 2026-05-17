package com.iletiniz.exception;

/** HTTP 401 — geçersiz veya iptal edilmiş API anahtarı. */
public class IletinizAuthenticationException extends IletinizApiException {

    public IletinizAuthenticationException(String message, int status, String code, String body, String requestId) {
        super(message, status, code, body, requestId);
    }
}
