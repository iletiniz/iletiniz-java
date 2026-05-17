package com.iletiniz.exception;

/** HTTP 404. */
public class IletinizNotFoundException extends IletinizApiException {

    public IletinizNotFoundException(String message, int status, String code, String body, String requestId) {
        super(message, status, code, body, requestId);
    }
}
