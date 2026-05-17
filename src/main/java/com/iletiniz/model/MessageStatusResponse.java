package com.iletiniz.model;

import com.google.gson.annotations.SerializedName;

/** {@code GET /v1/messages/{jobId}} yanıtı. */
public record MessageStatusResponse(
        @SerializedName("job_id") String jobId,
        MessageStatus status,
        String to,
        String provider,
        ErrorInfo error,
        @SerializedName("created_at") String createdAt,
        @SerializedName("sent_at") String sentAt,
        @SerializedName("delivered_at") String deliveredAt
) {
}
