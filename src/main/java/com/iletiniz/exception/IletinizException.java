package com.iletiniz.exception;

/**
 * Tüm Iletiniz SDK hatalarının taban sınıfı.
 */
public class IletinizException extends RuntimeException {

    public IletinizException(String message) {
        super(message);
    }

    public IletinizException(String message, Throwable cause) {
        super(message, cause);
    }
}
