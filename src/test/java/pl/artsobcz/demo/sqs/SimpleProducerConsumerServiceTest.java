package pl.artsobcz.demo.sqs;

import org.awaitility.Awaitility;
import org.awaitility.Durations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;
import static pl.artsobcz.demo.sqs.ConsumerService.FANOUT_QUEUE_NAME;
import static pl.artsobcz.demo.sqs.ProducerService.OUTPUT_QUEUE_NAME;

public class SimpleProducerConsumerServiceTest extends BaseIntegrationTest {

    @Autowired
    private ConsumerService consumerService;

    @Autowired
    private ProducerService producerService;

    @Autowired
    private DemoMessageRepository demoMessageRepository;

    @BeforeEach
    public void beforeEach() {
        demoMessageRepository.clear();
    }

    @Test
    void test_receiveSQSListener() {

        // given
        DemoMessage testMessage = DemoMessage.builder()
                .key("joke")
                .body("What made the Java developers wear glasses?... They can't C.")
                .uuid(UUID.randomUUID())
                .build();

        // when
        sqsTemplate.send(ConsumerService.INPUT_QUEUE_NAME, testMessage);


        // then
        Awaitility.await()
                .atMost(Durations.ONE_SECOND)
                .pollInterval(Durations.ONE_HUNDRED_MILLISECONDS)
                .until(() -> demoMessageRepository.getAll().contains(testMessage));
    }

    @Test
    void test_sendToSns() {

        // given
        DemoMessage testMessage = DemoMessage.builder()
                .key("joke")
                .body("What is a Linux user's favorite game?... sudo ku")
                .uuid(UUID.randomUUID())
                .build();

        // when
        producerService.sendToTopic(testMessage);

        // then
        Awaitility.await()
                .atMost(Durations.ONE_SECOND)
                .pollInterval(Durations.ONE_HUNDRED_MILLISECONDS)
                .until(() -> consumerService.receiveMessage(FANOUT_QUEUE_NAME).get().getPayload(), equalTo(testMessage));
    }

    @Test
    void test_sendToSqsAndReceive() {

        // given
        DemoMessage testMessage = DemoMessage.builder()
                .key("joke")
                .body("Why do they call it hyper text?... Too much JAVA.")
                .uuid(UUID.randomUUID())
                .build();

        // when
        producerService.sendToQueue(testMessage);

        // then
        Awaitility.await()
                .pollInterval(Durations.ONE_HUNDRED_MILLISECONDS)
                .atMost(Durations.ONE_SECOND)
                .until(() -> consumerService.receiveMessage(OUTPUT_QUEUE_NAME).get().getPayload(), equalTo(testMessage));
    }
}
