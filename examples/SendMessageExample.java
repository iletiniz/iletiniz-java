import com.iletiniz.IletinizClient;
import com.iletiniz.model.SendMessageParams;
import com.iletiniz.model.SendMessageResponse;

public class SendMessageExample {
    public static void main(String[] args) {
        IletinizClient client = IletinizClient.builder()
                .apiKey(System.getenv("ILETINIZ_API_KEY"))
                .build();

        SendMessageResponse res = client.messages().send(
                SendMessageParams.builder()
                        .to("+905551234567")
                        .body("Merhaba! Bu Iletiniz SDK ile gönderilen test mesajıdır.")
                        .build()
        );

        System.out.println("Job: " + res.jobId() + " Status: " + res.status());
    }
}
