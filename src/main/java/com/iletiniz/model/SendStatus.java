package com.iletiniz.model;

import com.google.gson.annotations.SerializedName;

/** Tek mesaj gönderim sonucu. */
public enum SendStatus {
    @SerializedName("sent")
    SENT,
    @SerializedName("queued")
    QUEUED,
    @SerializedName("failed")
    FAILED
}
