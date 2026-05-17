package com.iletiniz.exception;

/** HTTP 403 — yetki yok. */
public class IletinizPermissionException extends IletinizApiException {

    public IletinizPermissionException(String message, int status, String code, String body, String requestId) {
        super(message, status, code, body, requestId);
    }
}
