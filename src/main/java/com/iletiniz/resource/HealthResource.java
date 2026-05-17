package com.iletiniz.resource;

import com.iletiniz.RequestOptions;
import com.iletiniz.http.InternalHttpClient;
import com.iletiniz.model.HealthResponse;

/** {@code /v1/health} endpoint'i. */
public final class HealthResource {

    private final InternalHttpClient http;

    public HealthResource(InternalHttpClient http) {
        this.http = http;
    }

    /** API ve veritabanının erişilebilirliğini kontrol eder. */
    public HealthResponse check() {
        return check(null);
    }

    /** Ek opsiyonlarla sağlık kontrolü. */
    public HealthResponse check(RequestOptions options) {
        return http.request("GET", "/v1/health", null, null, HealthResponse.class, options);
    }
}
