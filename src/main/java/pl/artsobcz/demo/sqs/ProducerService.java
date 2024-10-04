package pl.artsobcz.demo.sqs;


import io.awspring.cloud.sns.core.SnsTemplate;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProducerService {

    public final static String OUTPUT_QUEUE_NAME = "output-queue";
    public final static String OUTPUT_TOPIC_NAME = "output-topic";

    private final SqsTemplate sqsTemplate;
    private final SnsTemplate snsTemplate;

    public void sendToQueue(DemoMessage message) {
        sqsTemplate.send(to -> to.queue(OUTPUT_QUEUE_NAME)
                .payload(message));
    }

    public void sendToTopic(DemoMessage message) {
        snsTemplate.convertAndSend(OUTPUT_TOPIC_NAME, message);
    }
}
