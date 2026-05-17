package com.iletiniz.model;

/** API yanıtlarında dönen hata gövdesi. */
public record ErrorInfo(String code, String message) {
}
