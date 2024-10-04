package pl.artsobcz.demo.sqs;

import io.awspring.cloud.sqs.annotation.SqsListener;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;
import pl.artsobcz.demo.sqs.observibility.Traced;
import org.springframework.messaging.handler.annotation.Header;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsumerService {

    public final static String FANOUT_QUEUE_NAME = "fanout_queue";
    public final static String INPUT_QUEUE_NAME = "input-queue";

    private final DemoMessageRepository messageRepository;
    private final SqsTemplate sqsTemplate;

    @SqsListener(queueNames = INPUT_QUEUE_NAME)
    @Traced
    public void onMessage(DemoMessage message, @Header("traceparent") String traceparent) {
        try {
            log.info("received message: {}", message);
            messageRepository.save(message);
        } catch (Exception e) {
            log.error("error processing message: {}", message, e);
        }
    }

    public Optional<Message<DemoMessage>> receiveMessage(String queueName) {
        return sqsTemplate.receive(queueName, DemoMessage.class);
    }
}
