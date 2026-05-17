package com.iletiniz.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Toplu gönderimde tek bir mesaj öğesi. */
public final class BulkItemInput {

    private final String to;
    private final String body;
    private final Map<String, Object> variables;

    private BulkItemInput(Builder b) {
        this.to = b.to;
        this.body = b.body;
        this.variables = b.variables == null ? null : Collections.unmodifiableMap(new LinkedHashMap<>(b.variables));
    }

    public String getTo() { return to; }
    public String getBody() { return body; }
    public Map<String, Object> getVariables() { return variables; }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String to;
        private String body;
        private Map<String, Object> variables;

        private Builder() {}

        public Builder to(String to) { this.to = to; return this; }
        public Builder body(String body) { this.body = body; return this; }

        public Builder variables(Map<String, Object> variables) {
            this.variables = variables == null ? null : new LinkedHashMap<>(variables);
            return this;
        }

        public Builder variable(String key, Object value) {
            if (this.variables == null) this.variables = new LinkedHashMap<>();
            this.variables.put(key, value);
            return this;
        }

        public BulkItemInput build() { return new BulkItemInput(this); }
    }
}
