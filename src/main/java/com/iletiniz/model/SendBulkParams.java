package com.iletiniz.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Toplu gönderim parametreleri.
 *
 * <p>{@code template} verildiyse her item'da {@code body} olmamalı (yalnızca {@code variables} opsiyonel).
 * {@code template} yoksa her item'da {@code body} zorunludur, {@code variables} kullanılamaz.
 */
public final class SendBulkParams {

    private final String provider;
    private final String sender;
    private final String template;
    private final Boolean iys;
    private final List<BulkItemInput> items;

    private SendBulkParams(Builder b) {
        this.provider = b.provider;
        this.sender = b.sender;
        this.template = b.template;
        this.iys = b.iys;
        this.items = b.items == null ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(b.items));
    }

    public String getProvider() { return provider; }
    public String getSender() { return sender; }
    public String getTemplate() { return template; }
    public Boolean getIys() { return iys; }
    public List<BulkItemInput> getItems() { return items; }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String provider;
        private String sender;
        private String template;
        private Boolean iys;
        private List<BulkItemInput> items;

        private Builder() {}

        public Builder provider(String provider) { this.provider = provider; return this; }
        public Builder sender(String sender) { this.sender = sender; return this; }
        public Builder template(String template) { this.template = template; return this; }

        /** İYS izni — bkz. {@link SendMessageParams.Builder#iys(Boolean)}. Tüm batch için tek değer. */
        public Builder iys(Boolean iys) { this.iys = iys; return this; }

        public Builder items(List<BulkItemInput> items) {
            this.items = items == null ? null : new ArrayList<>(items);
            return this;
        }

        public Builder addItem(BulkItemInput item) {
            if (this.items == null) this.items = new ArrayList<>();
            this.items.add(item);
            return this;
        }

        public SendBulkParams build() { return new SendBulkParams(this); }
    }
}
