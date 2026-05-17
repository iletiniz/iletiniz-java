package com.iletiniz;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** İstek bazlı opsiyonlar (timeout, ek başlıklar). */
public final class RequestOptions {

    private final Integer timeoutMs;
    private final Map<String, String> headers;

    private RequestOptions(Builder b) {
        this.timeoutMs = b.timeoutMs;
        this.headers = b.headers == null ? null : Collections.unmodifiableMap(new LinkedHashMap<>(b.headers));
    }

    public Integer getTimeoutMs() { return timeoutMs; }
    public Map<String, String> getHeaders() { return headers; }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private Integer timeoutMs;
        private Map<String, String> headers;

        private Builder() {}

        /** Bu isteğe özel timeout (ms). Client default'unu ezer. */
        public Builder timeoutMs(int timeoutMs) {
            this.timeoutMs = timeoutMs;
            return this;
        }

        /** Bu isteğe ek HTTP başlıkları. */
        public Builder headers(Map<String, String> headers) {
            this.headers = headers == null ? null : new LinkedHashMap<>(headers);
            return this;
        }

        /** Tek bir başlık ekler. */
        public Builder header(String name, String value) {
            if (this.headers == null) this.headers = new LinkedHashMap<>();
            this.headers.put(name, value);
            return this;
        }

        public RequestOptions build() { return new RequestOptions(this); }
    }
}
