package com.iletiniz;

import com.iletiniz.exception.IletinizException;
import com.iletiniz.http.InternalHttpClient;
import com.iletiniz.http.JdkHttpTransport;
import com.iletiniz.http.Transport;
import com.iletiniz.resource.HealthResource;
import com.iletiniz.resource.MessagesResource;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Iletiniz API'sine erişim sağlayan ana istemci.
 *
 * <pre>{@code
 * IletinizClient client = IletinizClient.builder()
 *     .apiKey(System.getenv("ILETINIZ_API_KEY"))
 *     .build();
 *
 * SendMessageResponse res = client.messages().send(
 *     SendMessageParams.builder()
 *         .to("+905551234567")
 *         .body("Merhaba!")
 *         .build()
 * );
 * }</pre>
 */
public final class IletinizClient {

    public static final String VERSION = "0.1.0";

    private static final String DEFAULT_BASE_URL = "https://api.iletiniz.com";
    private static final int DEFAULT_TIMEOUT_MS = 30_000;
    private static final int DEFAULT_MAX_RETRIES = 2;
    private static final Pattern API_KEY_RE = Pattern.compile("^iltz_(?:live|test)_[A-Za-z0-9_-]+$");

    private final MessagesResource messages;
    private final HealthResource health;

    private IletinizClient(Builder b) {
        String apiKey = b.apiKey != null ? b.apiKey : System.getenv("ILETINIZ_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IletinizException(
                    "API anahtarı gerekli. IletinizClient.builder().apiKey(...) "
                            + "veya ILETINIZ_API_KEY ortam değişkeni kullanın.");
        }
        if (!API_KEY_RE.matcher(apiKey).matches()) {
            throw new IletinizException(
                    "Geçersiz API anahtar formatı. Beklenen: 'iltz_live_…' veya 'iltz_test_…'.");
        }

        String baseUrl = b.baseUrl;
        if (baseUrl == null || baseUrl.isEmpty()) {
            String envBase = System.getenv("ILETINIZ_BASE_URL");
            baseUrl = (envBase != null && !envBase.isEmpty()) ? envBase : DEFAULT_BASE_URL;
        }

        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("User-Agent", "iletiniz-java/" + VERSION);
        if (b.defaultHeaders != null) {
            headers.putAll(b.defaultHeaders);
        }

        Transport transport = b.transport != null ? b.transport : new JdkHttpTransport();

        InternalHttpClient http = new InternalHttpClient(
                baseUrl,
                apiKey,
                b.timeoutMs > 0 ? b.timeoutMs : DEFAULT_TIMEOUT_MS,
                b.maxRetries >= 0 ? b.maxRetries : DEFAULT_MAX_RETRIES,
                headers,
                transport
        );

        this.messages = new MessagesResource(http);
        this.health = new HealthResource(http);
    }

    /** Mesaj gönderim ve durum sorgulama servisi. */
    public MessagesResource messages() { return messages; }

    /** Sağlık kontrolü servisi. */
    public HealthResource health() { return health; }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String apiKey;
        private String baseUrl;
        private int timeoutMs = DEFAULT_TIMEOUT_MS;
        private int maxRetries = DEFAULT_MAX_RETRIES;
        private Map<String, String> defaultHeaders;
        private Transport transport;

        private Builder() {}

        /** API anahtarı ({@code iltz_live_…} ya da {@code iltz_test_…}). */
        public Builder apiKey(String apiKey) { this.apiKey = apiKey; return this; }

        /** API base URL'sini değiştirir. Varsayılan: https://api.iletiniz.com */
        public Builder baseUrl(String baseUrl) { this.baseUrl = baseUrl; return this; }

        /** İstekler için varsayılan timeout (ms). Varsayılan: 30000. */
        public Builder timeoutMs(int timeoutMs) { this.timeoutMs = timeoutMs; return this; }

        /** Geçici hatalarda yeniden deneme sayısı. Varsayılan: 2. */
        public Builder maxRetries(int maxRetries) { this.maxRetries = maxRetries; return this; }

        /** Her isteğe eklenecek varsayılan başlıklar. */
        public Builder defaultHeaders(Map<String, String> headers) {
            this.defaultHeaders = headers == null ? null : new LinkedHashMap<>(headers);
            return this;
        }

        /** Test/proxy için özel transport implementasyonu. */
        public Builder transport(Transport transport) { this.transport = transport; return this; }

        public IletinizClient build() { return new IletinizClient(this); }
    }
}
