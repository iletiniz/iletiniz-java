import com.iletiniz.IletinizClient;
import com.iletiniz.model.SendMessageParams;
import com.iletiniz.model.SendMessageResponse;

public class SendTemplateExample {
    public static void main(String[] args) {
        IletinizClient client = IletinizClient.builder()
                .apiKey(System.getenv("ILETINIZ_API_KEY"))
                .build();

        SendMessageResponse res = client.messages().send(
                SendMessageParams.builder()
                        .to("+905551234567")
                        .template("order_shipped")
                        .variable("name", "Ayşe")
                        .variable("tracking_no", "TR123456789")
                        .build()
        );

        System.out.println("Sent via template: " + res.templateKey() + " -> " + res.status());
    }
}
