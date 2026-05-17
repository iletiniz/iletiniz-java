package com.iletiniz.exception;

/** Ağ kaynaklı bağlantı hatası. */
public class IletinizConnectionException extends IletinizException {

    public IletinizConnectionException(String message) {
        super(message);
    }

    public IletinizConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
