# İletiniz Java SDK

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](./LICENSE)

Iletiniz API için resmi Java SDK'si. Java 17+ üzerinde çalışır.

## Kurulum

### Maven

```xml
<dependency>
    <groupId>com.iletiniz</groupId>
    <artifactId>sdk</artifactId>
    <version>0.1.0</version>
</dependency>
```

### Gradle (Kotlin DSL)

```kotlin
implementation("com.iletiniz:sdk:0.1.0")
```

Gereksinimler:

- Java `>= 17`

## Hızlı başlangıç

```java
import com.iletiniz.IletinizClient;
import com.iletiniz.model.SendMessageParams;
import com.iletiniz.model.SendMessageResponse;

IletinizClient client = IletinizClient.builder()
        .apiKey(System.getenv("ILETINIZ_API_KEY")) // 'iltz_live_…' veya 'iltz_test_…'
        .build();

SendMessageResponse res = client.messages().send(
        SendMessageParams.builder()
                .to("+905551234567")
                .body("Merhaba!")
                .build()
);

System.out.println(res.jobId() + " " + res.status());
```

`apiKey` boş bırakıldığında SDK `ILETINIZ_API_KEY` ortam değişkenini okur.

## Yapılandırma

```java
IletinizClient client = IletinizClient.builder()
        .apiKey("iltz_live_…")
        .baseUrl("https://api.iletiniz.com")           // varsayılan
        .timeoutMs(30_000)                              // varsayılan
        .maxRetries(2)                                  // 408/429/5xx ve ağ hatalarında
        .defaultHeaders(Map.of("X-Source", "crm"))
        .transport(new JdkHttpTransport())              // özel Transport implementasyonu
        .build();
```

## Endpoint'ler

| Metot                                            | HTTP                              |
| ------------------------------------------------ | --------------------------------- |
| `client.health().check()`                        | `GET /v1/health`                  |
| `client.messages().send(params)`                 | `POST /v1/messages`               |
| `client.messages().sendBulk(params)`             | `POST /v1/messages/bulk`          |
| `client.messages().retrieve(jobId)`              | `GET /v1/messages/{jobId}`        |
| `client.messages().status(jobId)` (alias)        | `GET /v1/messages/{jobId}`        |

### Tek mesaj göndermek

```java
client.messages().send(
        SendMessageParams.builder()
                .to("+905551234567")
                .body("Sipariş kodunuz: 4821")
                .sender("MAGAZA")     // opsiyonel
                .provider("netgsm")   // opsiyonel
                .build()
);
```

### Telegram üzerinden göndermek

`provider("telegram")` seçildiğinde `to` alanı SMS yerine Telegram alıcı tanımlayıcısı bekler:
numerik `chat_id` (örn `8409353994`, gruplar için `-1001234567890`) veya `@kullaniciadi`. `sender` Telegram için kullanılmaz — bot kimliği bağlantıdaki token'a gömülüdür.

```java
client.messages().send(
        SendMessageParams.builder()
                .to("8409353994")
                .body("Merhaba!")
                .provider("telegram")
                .build()
);
```

### Template ile göndermek

```java
client.messages().send(
        SendMessageParams.builder()
                .to("+905551234567")
                .template("order_shipped")
                .variable("name", "Ayşe")
                .variable("tracking_no", "TR123")
                .build()
);
```

`body` ve `template` aynı anda kullanılamaz; tam olarak biri zorunludur. `variables` yalnızca `template` ile birlikte verilebilir.

### Toplu gönderim

Tek istekte en fazla 200 öğe gönderebilirsiniz.

```java
// Düz metin modu — her item'da body zorunlu, variables yok
client.messages().sendBulk(
        SendBulkParams.builder()
                .addItem(BulkItemInput.builder().to("+905551111111").body("Mesaj 1").build())
                .addItem(BulkItemInput.builder().to("+905552222222").body("Mesaj 2").build())
                .build()
);

// Template modu — items'ta body olmamalı
client.messages().sendBulk(
        SendBulkParams.builder()
                .template("low_stock_alert")
                .addItem(BulkItemInput.builder()
                        .to("+905551111111")
                        .variable("product", "Ürün A")
                        .variable("stock", 3)
                        .build())
                .addItem(BulkItemInput.builder()
                        .to("+905552222222")
                        .variable("product", "Ürün B")
                        .variable("stock", 1)
                        .build())
                .build()
);
```

### Mesaj durumunu sorgulamak

```java
MessageStatusResponse info = client.messages().retrieve(jobId);
// info.status(): MessageStatus.SENT | QUEUED | FAILED | DELIVERED | EXPIRED | REJECTED | UNKNOWN
```

### Sağlık kontrolü

```java
HealthResponse health = client.health().check();
// health.ok() == true, health.db() == "up"
```

## Hata yönetimi

Tüm hatalar `IletinizException` sınıfından türetilir. HTTP status'a göre uygun alt sınıf fırlatılır:

```java
import com.iletiniz.exception.*;

try {
    client.messages().send(params);
} catch (IletinizAuthenticationException e) {
    // 401 — geçersiz veya iptal edilmiş anahtar
} catch (IletinizValidationException e) {
    // 400 / 422 — istek doğrulanamadı
    System.err.println(e.getBody());
} catch (IletinizRateLimitException e) {
    // 429 — yeniden denemeden önce backoff
} catch (IletinizNotFoundException e) {
    // 404
} catch (IletinizServerException e) {
    // 5xx
} catch (IletinizApiException e) {
    System.err.printf("%d %s %s [%s]%n",
            e.getStatus(), e.getCode(), e.getMessage(), e.getRequestId());
} catch (IletinizTimeoutException e) {
    // istek timeout'a takıldı
} catch (IletinizConnectionException e) {
    // ağ hatası
}
```

## Yeniden deneme stratejisi

SDK, aşağıdaki durumlarda otomatik olarak `maxRetries` defa yeniden dener (varsayılan: 2):

- Ağ kaynaklı bağlantı hataları
- HTTP 408, 429, 500–599

`Retry-After` başlığı varsa beklenir; aksi halde exponential backoff (jitter ile) uygulanır. Yeniden denemeyi kapatmak için `maxRetries(0)` verin.

## Timeout

İstek bazlı timeout:

```java
import com.iletiniz.RequestOptions;

client.messages().send(
        SendMessageParams.builder().to("+905551234567").body("merhaba").build(),
        RequestOptions.builder().timeoutMs(10_000).build()
);
```

## Test

SDK, `com.iletiniz.http.Transport` arayüzü üzerinden HTTP katmanını dışarı açar. Testlerinizde gerçek ağ trafiği oluşturmadan SDK'yı kullanabilirsiniz:

```java
Transport fake = (method, url, headers, body, timeoutMs) ->
        new HttpResponse(200, "{\"ok\":true,\"db\":\"up\"}", Map.of());

IletinizClient client = IletinizClient.builder()
        .apiKey("iltz_test_xxx")
        .transport(fake)
        .build();
```

## Katkıda Bulunma / Contributing

Katkı sağlamak ister misiniz? Lütfen [CONTRIBUTING.md](./CONTRIBUTING.md) dosyasını inceleyin. English: [CONTRIBUTING.en.md](./CONTRIBUTING.en.md).

## Davranış Kuralları / Code of Conduct

Bu proje [Contributor Covenant](./CODE_OF_CONDUCT.md) davranış kurallarına bağlıdır. English: [CODE_OF_CONDUCT.en.md](./CODE_OF_CONDUCT.en.md).

## Güvenlik / Security

Güvenlik açığı bildirmek için lütfen [SECURITY.md](./SECURITY.md) dosyasındaki adımları izleyin — **public issue açmayın**. English: [SECURITY.en.md](./SECURITY.en.md).

## Lisans / License

MIT — bkz. / see [LICENSE](./LICENSE).
