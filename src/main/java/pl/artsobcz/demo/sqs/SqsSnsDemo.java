package pl.artsobcz.demo.sqs;

import io.awspring.cloud.sqs.MessageHeaderUtils;
import io.awspring.cloud.sqs.support.converter.MessageConversionContext;
import io.awspring.cloud.sqs.support.converter.SnsMessageConverter;
import io.awspring.cloud.sqs.support.converter.SqsMessagingMessageConverter;
import io.micrometer.observation.NullObservation;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.GenericMessage;
import software.amazon.awssdk.services.sqs.model.Message;

import java.util.HashMap;
import java.util.Map;

/**
 * - https://www.baeldung.com/java-spring-cloud-aws-v3-intro
 */

@SpringBootApplication
@EnableAspectJAutoProxy
public class SqsSnsDemo {

    public static void main(String[] args) {
        SpringApplication.run(SqsSnsDemo.class, args);
    }

    @Bean
    public ObservationRegistry observationRegistry() {
        return ObservationRegistry.create();
    }

//    @Bean
//    public CommandLineRunner runner() {
//
//        return (args) -> {
//            Observation.createNotStarted("on-message", observationRegistry()).observe(() -> {
//                log.info("Hello World");
//            });
//        };
//    }
}
