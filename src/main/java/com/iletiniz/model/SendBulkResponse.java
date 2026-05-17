package com.iletiniz.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/** {@code POST /v1/messages/bulk} yanıtı. */
public record SendBulkResponse(
        int total,
        int sent,
        int failed,
        String provider,
        String template,
        @SerializedName("template_key") String templateKey,
        @SerializedName("created_at") String createdAt,
        List<SendBulkItemResult> results
) {
}
