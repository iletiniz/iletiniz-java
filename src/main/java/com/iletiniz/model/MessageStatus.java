package com.iletiniz.model;

import com.google.gson.annotations.SerializedName;

/** Mesajın olası nihai durumları. */
public enum MessageStatus {
    @SerializedName("sent")
    SENT,
    @SerializedName("queued")
    QUEUED,
    @SerializedName("failed")
    FAILED,
    @SerializedName("delivered")
    DELIVERED,
    @SerializedName("expired")
    EXPIRED,
    @SerializedName("rejected")
    REJECTED,
    @SerializedName("unknown")
    UNKNOWN
}
