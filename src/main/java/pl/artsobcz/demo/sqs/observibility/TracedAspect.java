package pl.artsobcz.demo.sqs.observibility;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class TracedAspect {

    private final Tracer tracer;

    @Around("@annotation(Traced)")
    public Object observeMethod(ProceedingJoinPoint joinPoint) throws Throwable {

        String[] arguments = joinPoint.getArgs()[1].toString().split("-");
        TraceContext traceContext = tracer.traceContextBuilder()
                .traceId(arguments[0])
                .spanId(arguments[1])
                .build();

        Span span = tracer.spanBuilder().setParent(traceContext).start();

        try (Tracer.SpanInScope spanInScope = tracer.withSpan(span)) {
            return joinPoint.proceed();
        } finally {
            span.end();
        }
    }
}
