package com.iletiniz.model;

/** {@code GET /v1/health} yanıtı. */
public record HealthResponse(boolean ok, String db) {
}
