package pl.artsobcz.demo.sqs;


import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.Awaitility;
import org.awaitility.Durations;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static pl.artsobcz.demo.sqs.ConsumerService.FANOUT_QUEUE_NAME;

@Slf4j
public class TracingConsumerServiceTest extends BaseIntegrationTest {

    @Autowired
    private ProducerService producerService;

    @Autowired
    private DemoMessageRepository demoMessageRepository;

    @Autowired
    private ObservationRegistry observationRegistry;

    private ListAppender<ILoggingEvent> appender;

    @BeforeEach
    void setup() {
        appender = new ListAppender<>();
        appender.start();
        ((Logger) LoggerFactory.getLogger(ConsumerService.class)).addAppender(appender);
    }

    @BeforeEach
    public void beforeEach() {
        demoMessageRepository.clear();
    }

    @AfterEach
    public void tearDown() {
        ((Logger) LoggerFactory.getLogger(ConsumerService.class)).detachAppender(appender);
    }

    @Test
    void test_receiveSQSListener_withTraceId() {

        // given
        DemoMessage testMessage = DemoMessage.builder()
                .key("joke")
                .body("What made the Java developers wear glasses?... They can't C.")
                .uuid(UUID.randomUUID())
                .build();

        // when
        String expectedTraceId = Observation.createNotStarted("test", observationRegistry).observeChecked(
                () -> {
                    log.info("test_receiveSQSListener_withTraceId - sending test message {}", testMessage);
                    sqsTemplate.send(ConsumerService.INPUT_QUEUE_NAME, testMessage);
                    return MDC.get("traceId");
                }
        );

        // then
        Awaitility.await()
                .pollInterval(Durations.ONE_HUNDRED_MILLISECONDS)
                .until(() -> demoMessageRepository.getAll().contains(testMessage));

        // traceId in log
        assertEquals(expectedTraceId, appender.list.get(0).getMDCPropertyMap().get("traceId"));
    }

    @Test
    void test_sendToSns_withTraceId() {

        // given
        DemoMessage testMessage = DemoMessage.builder()
                .key("joke")
                .body("What is a Linux user's favorite game?... sudo ku")
                .uuid(UUID.randomUUID())
                .build();

        // when
        String expectedTraceId = Observation.createNotStarted("test", observationRegistry).observeChecked(
                () -> {
                    log.info("test_sendToSns_withTraceId - sending test message {}", testMessage);
                    producerService.sendToTopic(testMessage);
                    return MDC.get("traceId");
                }
        );

        // then
        Optional<Message<DemoMessage>> messageOpt =  Awaitility.await()
                .pollInterval(Durations.ONE_HUNDRED_MILLISECONDS)
                .atMost(Durations.ONE_SECOND)
                .until(() -> sqsTemplate.receive(FANOUT_QUEUE_NAME, DemoMessage.class), Optional::isPresent);


        assertEquals(testMessage, messageOpt.get().getPayload());

        // traceId from message attribute
        log.info("test_sendToSns_withTraceId - received `traceparent` attribute: {}", messageOpt.get().getHeaders().get("traceparent"));
        assertEquals(expectedTraceId, messageOpt.get().getHeaders().get("traceparent").toString().split("-")[0]);
    }
}
