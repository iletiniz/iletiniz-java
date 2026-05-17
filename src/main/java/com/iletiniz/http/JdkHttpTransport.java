package com.iletiniz.http;

import com.iletiniz.exception.IletinizConnectionException;
import com.iletiniz.exception.IletinizTimeoutException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** {@link java.net.http.HttpClient} üzerine kurulu varsayılan transport. */
public final class JdkHttpTransport implements Transport {

    private final HttpClient httpClient;

    public JdkHttpTransport() {
        this(HttpClient.newBuilder().build());
    }

    public JdkHttpTransport(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public HttpResponse send(
            String method,
            String url,
            Map<String, String> headers,
            byte[] body,
            int timeoutMs
    ) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofMillis(timeoutMs));

            HttpRequest.BodyPublisher publisher = body == null
                    ? BodyPublishers.noBody()
                    : BodyPublishers.ofByteArray(body);
            builder.method(method, publisher);

            for (Map.Entry<String, String> e : headers.entrySet()) {
                builder.header(e.getKey(), e.getValue());
            }

            java.net.http.HttpResponse<String> resp = httpClient.send(
                    builder.build(),
                    BodyHandlers.ofString()
            );

            Map<String, String> respHeaders = new LinkedHashMap<>();
            for (Map.Entry<String, List<String>> e : resp.headers().map().entrySet()) {
                if (e.getValue() != null && !e.getValue().isEmpty()) {
                    respHeaders.put(e.getKey(), e.getValue().get(0));
                }
            }

            return new HttpResponse(resp.statusCode(), resp.body(), respHeaders);
        } catch (HttpTimeoutException e) {
            throw new IletinizTimeoutException("İstek " + timeoutMs + "ms içinde tamamlanamadı.", e);
        } catch (IOException e) {
            throw new IletinizConnectionException(
                    e.getMessage() == null ? "bağlantı hatası" : e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IletinizConnectionException("istek kesintiye uğradı", e);
        }
    }
}
