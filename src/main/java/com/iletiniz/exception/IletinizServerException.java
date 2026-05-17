package com.iletiniz.exception;

/** HTTP 5xx. */
public class IletinizServerException extends IletinizApiException {

    public IletinizServerException(String message, int status, String code, String body, String requestId) {
        super(message, status, code, body, requestId);
    }
}
