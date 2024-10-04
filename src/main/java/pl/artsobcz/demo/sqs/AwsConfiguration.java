package pl.artsobcz.demo.sqs;

import io.awspring.cloud.sqs.MessageHeaderUtils;
import io.awspring.cloud.sqs.support.converter.MessageConversionContext;
import io.awspring.cloud.sqs.support.converter.SqsMessagingMessageConverter;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.GenericMessage;
import software.amazon.awssdk.services.sqs.model.Message;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class AwsConfiguration {

    @Bean
    SqsMessagingMessageConverter sqsMessagingMessageConverter(Tracer tracing) {
        return new SqsMessagingMessageConverter() {

            @Override
            public Message fromMessagingMessage(org.springframework.messaging.Message<?> message, MessageConversionContext context) {
                Span span = tracing.nextSpan().name("send").remoteServiceName("sqs").start();
                Map<String, Object> newHeaders =
                        Map.of(
                                "traceparent",
                                span.context().traceId() + "-" + span.context().spanId());
                span.end();
                return super.fromMessagingMessage(
                        MessageHeaderUtils.addHeadersIfAbsent(message, newHeaders), context);
            }
        };
    }

    @Bean
    ChannelInterceptor channelInterceptor(Tracer tracing) {
        return new ChannelInterceptor() {
            @Override
            public org.springframework.messaging.Message<?> preSend(org.springframework.messaging.Message<?> message, MessageChannel channel) {
                Span span = tracing.nextSpan().name("send").remoteServiceName("sns").start();
                String traceparent = span.context().traceId() + "-" + span.context().spanId();
                span.end();
                Map<String, Object> headers = new HashMap<>(message.getHeaders());
                headers.put("traceparent", traceparent);
                return new GenericMessage<>(message.getPayload(), headers);
            }
        };
    }

}
