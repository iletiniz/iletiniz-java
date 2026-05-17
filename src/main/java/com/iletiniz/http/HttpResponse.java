package com.iletiniz.http;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Transport tarafından döndürülen ham HTTP yanıtı. */
public final class HttpResponse {

    private final int status;
    private final String body;
    private final Map<String, String> headers;

    /**
     * @param headers anahtarları küçük harfe normalize edilmiş header'lar.
     */
    public HttpResponse(int status, String body, Map<String, String> headers) {
        this.status = status;
        this.body = body == null ? "" : body;
        Map<String, String> normalized = new LinkedHashMap<>();
        if (headers != null) {
            for (Map.Entry<String, String> e : headers.entrySet()) {
                if (e.getKey() != null) {
                    normalized.put(e.getKey().toLowerCase(java.util.Locale.ROOT), e.getValue());
                }
            }
        }
        this.headers = Collections.unmodifiableMap(normalized);
    }

    public int getStatus() { return status; }
    public String getBody() { return body; }
    public Map<String, String> getHeaders() { return headers; }

    public String getHeader(String name) {
        if (name == null) return null;
        return headers.get(name.toLowerCase(java.util.Locale.ROOT));
    }
}
