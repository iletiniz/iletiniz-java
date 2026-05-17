package com.iletiniz.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tek mesaj gönderim parametreleri.
 *
 * <p>{@code body} veya {@code template} alanlarından <strong>tam olarak biri</strong> verilmelidir.
 * {@code variables} yalnızca {@code template} ile birlikte kullanılabilir.
 */
public final class SendMessageParams {

    private final String to;
    private final String body;
    private final String template;
    private final Map<String, Object> variables;
    private final String sender;
    private final String provider;
    private final Boolean iys;

    private SendMessageParams(Builder b) {
        this.to = b.to;
        this.body = b.body;
        this.template = b.template;
        this.variables = b.variables == null ? null : Collections.unmodifiableMap(new LinkedHashMap<>(b.variables));
        this.sender = b.sender;
        this.provider = b.provider;
        this.iys = b.iys;
    }

    public String getTo() { return to; }
    public String getBody() { return body; }
    public String getTemplate() { return template; }
    public Map<String, Object> getVariables() { return variables; }
    public String getSender() { return sender; }
    public String getProvider() { return provider; }
    public Boolean getIys() { return iys; }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String to;
        private String body;
        private String template;
        private Map<String, Object> variables;
        private String sender;
        private String provider;
        private Boolean iys;

        private Builder() {}

        /** Alıcı telefon numarası (E.164 önerilir). */
        public Builder to(String to) { this.to = to; return this; }

        /** Düz metin gövde. {@code template} ile birlikte kullanılamaz. */
        public Builder body(String body) { this.body = body; return this; }

        /** Template anahtarı. {@code body} ile birlikte kullanılamaz. */
        public Builder template(String template) { this.template = template; return this; }

        /** Yalnızca {@code template} ile birlikte kullanılabilir. */
        public Builder variables(Map<String, Object> variables) {
            this.variables = variables == null ? null : new LinkedHashMap<>(variables);
            return this;
        }

        /** Tekil değişken ekler (variables map'i yoksa oluşturur). */
        public Builder variable(String key, Object value) {
            if (this.variables == null) this.variables = new LinkedHashMap<>();
            this.variables.put(key, value);
            return this;
        }

        /** Gönderici adı / başlık. */
        public Builder sender(String sender) { this.sender = sender; return this; }

        /** Belirli bir provider seçmek için kod. */
        public Builder provider(String provider) { this.provider = provider; return this; }

        /**
         * İYS izni. {@code true} → ticari mesaj (sağlayıcının İYS filtresi devreye girer).
         * {@code false} veya {@code null} → bilgilendirme (İYS sorgusu yok).
         * Yalnızca SMS sağlayıcılarında işlenir; WhatsApp/Telegram için yok sayılır.
         */
        public Builder iys(Boolean iys) { this.iys = iys; return this; }

        public SendMessageParams build() { return new SendMessageParams(this); }
    }
}
