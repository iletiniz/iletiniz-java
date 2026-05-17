package com.iletiniz.model;

import com.google.gson.annotations.SerializedName;

/** {@code POST /v1/messages} yanıtı. */
public record SendMessageResponse(
        @SerializedName("job_id") String jobId,
        SendStatus status,
        String to,
        String provider,
        String template,
        @SerializedName("template_key") String templateKey,
        ErrorInfo error,
        @SerializedName("created_at") String createdAt
) {
}
