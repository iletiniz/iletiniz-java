import com.iletiniz.IletinizClient;
import com.iletiniz.exception.IletinizNotFoundException;
import com.iletiniz.model.MessageStatusResponse;

public class StatusExample {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Kullanım: StatusExample <job_id>");
            System.exit(2);
        }

        IletinizClient client = IletinizClient.builder()
                .apiKey(System.getenv("ILETINIZ_API_KEY"))
                .build();

        try {
            MessageStatusResponse info = client.messages().retrieve(args[0]);
            System.out.println(info);
        } catch (IletinizNotFoundException e) {
            System.err.println("Mesaj bulunamadı.");
            System.exit(1);
        }
    }
}
