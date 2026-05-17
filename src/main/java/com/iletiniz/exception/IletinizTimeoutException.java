package com.iletiniz.exception;

/** İstek timeout süresinde tamamlanamadı. */
public class IletinizTimeoutException extends IletinizException {

    public IletinizTimeoutException(String message) {
        super(message);
    }

    public IletinizTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
