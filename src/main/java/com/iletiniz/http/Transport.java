package com.iletiniz.http;

import com.iletiniz.exception.IletinizConnectionException;
import com.iletiniz.exception.IletinizTimeoutException;

import java.util.Map;

/** Test ve özel HTTP istemcileri için injectable transport arayüzü. */
public interface Transport {

    /**
     * HTTP isteğini gönderir.
     *
     * @throws IletinizTimeoutException    İstek timeout süresinde tamamlanamadıysa.
     * @throws IletinizConnectionException Diğer ağ kaynaklı hatalar.
     */
    HttpResponse send(
            String method,
            String url,
            Map<String, String> headers,
            byte[] body,
            int timeoutMs
    );
}
