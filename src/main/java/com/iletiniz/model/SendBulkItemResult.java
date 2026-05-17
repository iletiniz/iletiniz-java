package com.iletiniz.model;

import com.google.gson.annotations.SerializedName;

/** Toplu gönderimde tek bir öğe için sonuç. */
public record SendBulkItemResult(
        String to,
        String status,
        @SerializedName("job_id") String jobId,
        ErrorInfo error
) {
}
