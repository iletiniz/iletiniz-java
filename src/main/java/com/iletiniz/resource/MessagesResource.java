package com.iletiniz.resource;

import com.iletiniz.RequestOptions;
import com.iletiniz.exception.IletinizException;
import com.iletiniz.http.InternalHttpClient;
import com.iletiniz.model.BulkItemInput;
import com.iletiniz.model.MessageStatusResponse;
import com.iletiniz.model.SendBulkParams;
import com.iletiniz.model.SendBulkResponse;
import com.iletiniz.model.SendMessageParams;
import com.iletiniz.model.SendMessageResponse;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** {@code /v1/messages} endpoint ailesi. */
public final class MessagesResource {

    private static final int MAX_BULK_ITEMS = 200;

    private final InternalHttpClient http;

    public MessagesResource(InternalHttpClient http) {
        this.http = http;
    }

    /**
     * Tek bir SMS mesajı gönderir.
     *
     * <p>{@link SendMessageParams#getBody()} veya {@link SendMessageParams#getTemplate()} alanlarından
     * <strong>tam olarak biri</strong> verilmelidir.
     * {@link SendMessageParams#getVariables()} yalnızca {@code template} ile birlikte kullanılabilir.
     */
    public SendMessageResponse send(SendMessageParams params) {
        return send(params, null);
    }

    public SendMessageResponse send(SendMessageParams params, RequestOptions options) {
        validateSendParams(params);
        return http.request(
                "POST",
                "/v1/messages",
                null,
                toJsonBody(params),
                SendMessageResponse.class,
                options
        );
    }

    /**
     * Tek istekte birden fazla mesaj gönderir (en fazla 200 öğe).
     *
     * <ul>
     *   <li>Üst seviye {@code template} verildiyse her item'da {@code body} olmamalı,
     *       yalnızca {@code variables} opsiyoneldir.</li>
     *   <li>Üst seviye {@code template} yoksa her item'da {@code body} zorunludur,
     *       {@code variables} kullanılamaz.</li>
     * </ul>
     */
    public SendBulkResponse sendBulk(SendBulkParams params) {
        return sendBulk(params, null);
    }

    public SendBulkResponse sendBulk(SendBulkParams params, RequestOptions options) {
        validateBulkParams(params);
        return http.request(
                "POST",
                "/v1/messages/bulk",
                null,
                toJsonBody(params),
                SendBulkResponse.class,
                options
        );
    }

    /** Daha önce gönderilmiş bir mesajın güncel durumunu döner. */
    public MessageStatusResponse retrieve(String jobId) {
        return retrieve(jobId, null);
    }

    public MessageStatusResponse retrieve(String jobId, RequestOptions options) {
        if (jobId == null || jobId.isEmpty()) {
            throw new IletinizException("jobId boş olamaz.");
        }
        String path = "/v1/messages/" + URLEncoder.encode(jobId, StandardCharsets.UTF_8);
        return http.request("GET", path, null, null, MessageStatusResponse.class, options);
    }

    /** {@link #retrieve(String)} için alias. */
    public MessageStatusResponse status(String jobId) {
        return retrieve(jobId);
    }

    public MessageStatusResponse status(String jobId, RequestOptions options) {
        return retrieve(jobId, options);
    }

    private static void validateSendParams(SendMessageParams p) {
        if (p == null) throw new IletinizException("send() parametre objesi gerektirir.");
        String to = p.getTo();
        if (to == null || to.length() < 7 || to.length() > 32) {
            throw new IletinizException("'to' alanı 7-32 karakter arasında olmalıdır.");
        }
        boolean hasBody = p.getBody() != null && !p.getBody().isEmpty();
        boolean hasTemplate = p.getTemplate() != null && !p.getTemplate().isEmpty();
        if (hasBody == hasTemplate) {
            throw new IletinizException("'body' veya 'template' alanlarından tam olarak biri zorunludur.");
        }
        if (p.getVariables() != null && !hasTemplate) {
            throw new IletinizException("'variables' yalnızca 'template' ile birlikte kullanılabilir.");
        }
        if (hasBody) {
            int len = p.getBody().length();
            if (len < 1 || len > 1600) {
                throw new IletinizException("'body' 1-1600 karakter arasında olmalıdır.");
            }
        }
    }

    private static void validateBulkParams(SendBulkParams p) {
        if (p == null) throw new IletinizException("sendBulk() parametre objesi gerektirir.");
        List<BulkItemInput> items = p.getItems();
        if (items == null || items.isEmpty()) {
            throw new IletinizException("'items' en az bir öğe içermelidir.");
        }
        if (items.size() > MAX_BULK_ITEMS) {
            throw new IletinizException("'items' en fazla " + MAX_BULK_ITEMS + " öğe içerebilir.");
        }
        boolean usingTemplate = p.getTemplate() != null && !p.getTemplate().isEmpty();
        for (int i = 0; i < items.size(); i++) {
            BulkItemInput item = items.get(i);
            if (item == null) {
                throw new IletinizException("items[" + i + "] null olamaz.");
            }
            String to = item.getTo();
            if (to == null || to.length() < 7 || to.length() > 32) {
                throw new IletinizException("items[" + i + "].to 7-32 karakter arasında olmalıdır.");
            }
            if (usingTemplate) {
                if (item.getBody() != null) {
                    throw new IletinizException(
                            "Üst seviye 'template' verildi: items[" + i + "].body kullanılamaz.");
                }
            } else {
                if (item.getBody() == null || item.getBody().isEmpty()) {
                    throw new IletinizException("'template' yok: items[" + i + "].body zorunludur.");
                }
                if (item.getVariables() != null) {
                    throw new IletinizException("'template' yok: items[" + i + "].variables kullanılamaz.");
                }
            }
        }
    }

    private static Map<String, Object> toJsonBody(SendMessageParams p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("to", p.getTo());
        if (p.getBody() != null) m.put("body", p.getBody());
        if (p.getTemplate() != null) m.put("template", p.getTemplate());
        if (p.getVariables() != null) m.put("variables", p.getVariables());
        if (p.getSender() != null) m.put("sender", p.getSender());
        if (p.getProvider() != null) m.put("provider", p.getProvider());
        if (p.getIys() != null) m.put("iys", p.getIys());
        return m;
    }

    private static Map<String, Object> toJsonBody(SendBulkParams p) {
        Map<String, Object> m = new LinkedHashMap<>();
        if (p.getProvider() != null) m.put("provider", p.getProvider());
        if (p.getSender() != null) m.put("sender", p.getSender());
        if (p.getTemplate() != null) m.put("template", p.getTemplate());
        if (p.getIys() != null) m.put("iys", p.getIys());
        java.util.List<Map<String, Object>> items = new java.util.ArrayList<>(p.getItems().size());
        for (BulkItemInput item : p.getItems()) {
            Map<String, Object> mi = new LinkedHashMap<>();
            mi.put("to", item.getTo());
            if (item.getBody() != null) mi.put("body", item.getBody());
            if (item.getVariables() != null) mi.put("variables", item.getVariables());
            items.add(mi);
        }
        m.put("items", items);
        return m;
    }
}
