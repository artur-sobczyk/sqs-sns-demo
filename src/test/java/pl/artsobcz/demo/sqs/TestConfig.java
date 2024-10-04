package pl.artsobcz.demo.sqs;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sqs.SqsClient;

@TestConfiguration
public class TestConfig {

    @Bean
    SqsClient sqsClient() {
        return BaseIntegrationTest.sqsClient();
    }

    @Bean
    SnsClient snsClient() {
        return BaseIntegrationTest.snsClient();
    }
}
