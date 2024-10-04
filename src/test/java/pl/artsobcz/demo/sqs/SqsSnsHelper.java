package pl.artsobcz.demo.sqs;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.CreateTopicResponse;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueResponse;
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesResponse;

import java.util.HashMap;
import java.util.Map;

import static software.amazon.awssdk.services.sqs.model.QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES;
import static software.amazon.awssdk.services.sqs.model.QueueAttributeName.QUEUE_ARN;

@Service
@RequiredArgsConstructor
public class SqsSnsHelper {

    private final Map<String, String> queueUrlMapping = new HashMap<>();
    private final SnsClient snsClient;
    private final SqsClient sqsClient;

    @PostConstruct
    public void createTestQueuesAndTopic() {

        CreateQueueResponse createQueueResponse1 = sqsClient.createQueue(builder -> builder.queueName(ConsumerService.INPUT_QUEUE_NAME));
        CreateQueueResponse createQueueResponse2 = sqsClient.createQueue(builder -> builder.queueName(ProducerService.OUTPUT_QUEUE_NAME));
        CreateQueueResponse createFanoutQueueResponse = sqsClient.createQueue(builder -> builder.queueName(ConsumerService.FANOUT_QUEUE_NAME));
        CreateTopicResponse createTopicResponse = snsClient.createTopic(builder -> builder.name(ProducerService.OUTPUT_TOPIC_NAME));

        GetQueueAttributesResponse fanoutQueueAttributes =
                sqsClient.getQueueAttributes(builder -> builder
                        .attributeNames(QUEUE_ARN)
                        .queueUrl(createFanoutQueueResponse.queueUrl()));

        snsClient.subscribe(builder -> builder
                .protocol("sqs")
                .attributes(Map.of("RawMessageDelivery", "true"))
                .endpoint(fanoutQueueAttributes.attributes().get(QUEUE_ARN))
                .topicArn(createTopicResponse.topicArn())
        );

        queueUrlMapping.put(ConsumerService.INPUT_QUEUE_NAME, createQueueResponse1.queueUrl());
        queueUrlMapping.put(ProducerService.OUTPUT_QUEUE_NAME, createQueueResponse2.queueUrl());
        queueUrlMapping.put(ConsumerService.FANOUT_QUEUE_NAME, createFanoutQueueResponse.queueUrl());
    }

    public void prune(String... queueNames) {

        for (String q : queueNames) {

            String url = queueUrlMapping.get(q);

            if (numOfMessages(url) > 0) {
                sqsClient.purgeQueue(builder -> builder.queueUrl(url));
            }
        }
    }

    private int numOfMessages(String url) {
        return Integer.parseInt(
                sqsClient.getQueueAttributes(
                                builder -> builder.attributeNames(APPROXIMATE_NUMBER_OF_MESSAGES).queueUrl(url))
                        .attributes()
                        .get(APPROXIMATE_NUMBER_OF_MESSAGES));
    }
}
