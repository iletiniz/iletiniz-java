package com.iletiniz.http;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.iletiniz.RequestOptions;
import com.iletiniz.exception.IletinizApiException;
import com.iletiniz.exception.IletinizAuthenticationException;
import com.iletiniz.exception.IletinizConnectionException;
import com.iletiniz.exception.IletinizNotFoundException;
import com.iletiniz.exception.IletinizPermissionException;
import com.iletiniz.exception.IletinizRateLimitException;
import com.iletiniz.exception.IletinizServerException;
import com.iletiniz.exception.IletinizTimeoutException;
import com.iletiniz.exception.IletinizValidationException;

import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Yüksek seviye HTTP istemcisi: retry, backoff, JSON encode/decode, hata haritalama.
 *
 * <p>Bu sınıf SDK iç kullanımı içindir; doğrudan tüketilmesi tavsiye edilmez.
 */
public final class InternalHttpClient {

    private static final Gson GSON = new Gson();

    private final String baseUrl;
    private final String apiKey;
    private final int timeoutMs;
    private final int maxRetries;
    private final Map<String, String> defaultHeaders;
    private final Transport transport;

    public InternalHttpClient(
            String baseUrl,
            String apiKey,
            int timeoutMs,
            int maxRetries,
            Map<String, String> defaultHeaders,
            Transport transport
    ) {
        this.baseUrl = stripTrailingSlash(baseUrl);
        this.apiKey = apiKey;
        this.timeoutMs = timeoutMs;
        this.maxRetries = maxRetries;
        this.defaultHeaders = defaultHeaders == null
                ? new LinkedHashMap<>()
                : new LinkedHashMap<>(defaultHeaders);
        this.transport = transport;
    }

    public Gson gson() {
        return GSON;
    }

    public <T> T request(
            String method,
            String path,
            Map<String, String> query,
            Object body,
            Type responseType,
            RequestOptions options
    ) {
        String url = buildUrl(path, query);

        Map<String, String> headers = new LinkedHashMap<>(defaultHeaders);
        headers.put("Authorization", "Bearer " + apiKey);
        headers.put("Accept", "application/json");
        if (options != null && options.getHeaders() != null) {
            headers.putAll(options.getHeaders());
        }

        byte[] payload = null;
        if (body != null) {
            headers.put("Content-Type", "application/json");
            payload = GSON.toJson(body).getBytes(StandardCharsets.UTF_8);
        }

        int effectiveTimeout = (options != null && options.getTimeoutMs() != null)
                ? options.getTimeoutMs()
                : timeoutMs;

        int attempt = 0;
        while (true) {
            HttpResponse response;
            try {
                response = transport.send(method, url, headers, payload, effectiveTimeout);
            } catch (IletinizTimeoutException | IletinizConnectionException e) {
                if (shouldRetry(null, attempt)) {
                    attempt++;
                    sleep(backoffMs(attempt, null));
                    continue;
                }
                throw e;
            }

            int status = response.getStatus();
            if (status >= 200 && status < 300) {
                if (status == 204 || response.getBody().isEmpty() || responseType == null) {
                    return null;
                }
                try {
                    return GSON.fromJson(response.getBody(), responseType);
                } catch (JsonSyntaxException e) {
                    throw new IletinizConnectionException("sunucudan geçersiz JSON döndü", e);
                }
            }

            if (shouldRetry(status, attempt)) {
                attempt++;
                sleep(backoffMs(attempt, response.getHeader("retry-after")));
                continue;
            }

            throw buildApiException(response);
        }
    }

    private String buildUrl(String path, Map<String, String> query) {
        String p = path.startsWith("/") ? path : "/" + path;
        StringBuilder sb = new StringBuilder(baseUrl).append(p);
        if (query != null && !query.isEmpty()) {
            sb.append('?');
            boolean first = true;
            for (Map.Entry<String, String> e : query.entrySet()) {
                if (e.getValue() == null) continue;
                if (!first) sb.append('&');
                first = false;
                sb.append(URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8))
                        .append('=')
                        .append(URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8));
            }
        }
        return sb.toString();
    }

    private boolean shouldRetry(Integer status, int attempt) {
        if (attempt >= maxRetries) return false;
        if (status == null) return true;
        if (status == 408 || status == 429) return true;
        return status >= 500 && status <= 599;
    }

    private long backoffMs(int attempt, String retryAfter) {
        if (retryAfter != null && !retryAfter.isEmpty()) {
            try {
                double sec = Double.parseDouble(retryAfter);
                if (sec > 0) {
                    return Math.min((long) (sec * 1000.0), 30_000L);
                }
            } catch (NumberFormatException ignored) {
                // expected: retry-after http-date or invalid
            }
        }
        long base = Math.min((long) Math.pow(2, attempt) * 250L, 4000L);
        return base + ThreadLocalRandom.current().nextInt(101);
    }

    private static void sleep(long ms) {
        if (ms <= 0) return;
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static String stripTrailingSlash(String s) {
        if (s == null) return "";
        int i = s.length();
        while (i > 0 && s.charAt(i - 1) == '/') i--;
        return s.substring(0, i);
    }

    private static IletinizApiException buildApiException(HttpResponse response) {
        int status = response.getStatus();
        String requestId = response.getHeader("x-request-id");
        String raw = response.getBody();

        String code = null;
        String message = null;

        if (raw != null && !raw.isEmpty()) {
            try {
                JsonObject obj = JsonParser.parseString(raw).getAsJsonObject();
                if (obj.has("error") && obj.get("error").isJsonPrimitive()) {
                    code = obj.get("error").getAsString();
                }
                if (obj.has("message") && obj.get("message").isJsonPrimitive()) {
                    message = obj.get("message").getAsString();
                }
            } catch (Exception ignored) {
                // body düz metin olabilir veya array olabilir.
                if (!raw.startsWith("{") && !raw.startsWith("[")) {
                    message = raw;
                }
            }
        }

        if (message == null || message.isEmpty()) {
            message = "HTTP " + status;
        }

        if (status == 401) return new IletinizAuthenticationException(message, status, code, raw, requestId);
        if (status == 403) return new IletinizPermissionException(message, status, code, raw, requestId);
        if (status == 404) return new IletinizNotFoundException(message, status, code, raw, requestId);
        if (status == 400 || status == 422) return new IletinizValidationException(message, status, code, raw, requestId);
        if (status == 429) return new IletinizRateLimitException(message, status, code, raw, requestId);
        if (status >= 500) return new IletinizServerException(message, status, code, raw, requestId);
        return new IletinizApiException(message, status, code, raw, requestId);
    }
}
