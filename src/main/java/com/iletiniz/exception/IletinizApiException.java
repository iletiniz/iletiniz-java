package com.iletiniz.exception;

/**
 * API tarafından dönen HTTP hatası.
 */
public class IletinizApiException extends IletinizException {

    private final int status;
    private final String code;
    private final String body;
    private final String requestId;

    public IletinizApiException(String message, int status, String code, String body, String requestId) {
        super(message);
        this.status = status;
        this.code = code;
        this.body = body;
        this.requestId = requestId;
    }

    /** HTTP status kodu. */
    public int getStatus() {
        return status;
    }

    /** API tarafından dönen makine-okunur hata kodu (varsa). */
    public String getCode() {
        return code;
    }

    /** API tarafından dönen ham gövde. */
    public String getBody() {
        return body;
    }

    /** Sunucu tarafında üretilen request id (varsa). */
    public String getRequestId() {
        return requestId;
    }
}
