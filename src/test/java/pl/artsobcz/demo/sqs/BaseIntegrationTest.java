package pl.artsobcz.demo.sqs;

import io.awspring.cloud.sns.core.SnsTemplate;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sqs.SqsClient;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SNS;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SQS;


/**
 * - https://github.com/awspring/spring-cloud-aws#compatibility-with-spring-project-versions
 * - https://java.testcontainers.org/supported_docker_environment/logging_config/
 * - https://github.com/awspring/spring-cloud-aws/discussions/902#discussioncomment-7188170
 */
@SpringBootTest
@ActiveProfiles(profiles = {"test"})
@Import(TestConfig.class)
public class BaseIntegrationTest {

    private static final String LOCAL_STACK_VERSION = "localstack/localstack:3.4.0";

    @Autowired
    private SqsSnsHelper sqsSnsHelper;

    @Autowired
    protected SqsTemplate sqsTemplate;

    @Autowired
    protected SnsTemplate snsTemplate;

    static LocalStackContainer localstack = new LocalStackContainer(DockerImageName.parse(LOCAL_STACK_VERSION))
            .withServices(SQS, SNS)
            .withReuse(true);

    @BeforeAll
    public static void beforeAll() {
        localstack.start();
    }

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.aws.region.static", () -> localstack.getRegion());
        registry.add("spring.cloud.aws.credentials.access-key", () -> localstack.getAccessKey());
        registry.add("spring.cloud.aws.credentials.secret-key", () -> localstack.getSecretKey());
        registry.add("spring.cloud.aws.sqs.endpoint", () -> localstack.getEndpointOverride(SQS).toString());
        registry.add("spring.cloud.aws.sns.endpoint", () -> localstack.getEndpointOverride(SNS).toString());
        registry.add("spring.cloud.aws.sqs.region", () -> localstack.getRegion());
        registry.add("spring.cloud.aws.sns.region", () -> localstack.getRegion());
    }

    static SqsClient sqsClient() {
        return SqsClient.builder()
                .endpointOverride(localstack.getEndpointOverride(SQS))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(localstack.getAccessKey(), localstack.getSecretKey())))
                .region(Region.of(localstack.getRegion()))
                .build();
    }

    static SnsClient snsClient() {
        return SnsClient.builder()
                .endpointOverride(localstack.getEndpointOverride(SNS))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(localstack.getAccessKey(), localstack.getSecretKey())))
                .region(Region.of(localstack.getRegion()))
                .build();
    }

    @BeforeEach
    public void beforeEach() {
        sqsSnsHelper.prune(ConsumerService.INPUT_QUEUE_NAME, ConsumerService.FANOUT_QUEUE_NAME, ProducerService.OUTPUT_QUEUE_NAME);
    }
}
