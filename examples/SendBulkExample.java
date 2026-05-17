import com.iletiniz.IletinizClient;
import com.iletiniz.model.BulkItemInput;
import com.iletiniz.model.SendBulkParams;
import com.iletiniz.model.SendBulkResponse;

public class SendBulkExample {
    public static void main(String[] args) {
        IletinizClient client = IletinizClient.builder()
                .apiKey(System.getenv("ILETINIZ_API_KEY"))
                .build();

        SendBulkResponse res = client.messages().sendBulk(
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

        System.out.printf("Toplam: %d, Gönderilen: %d, Başarısız: %d%n",
                res.total(), res.sent(), res.failed());
    }
}
