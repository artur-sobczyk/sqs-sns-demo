package pl.artsobcz.demo.sqs;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class DemoMessage {
    private String key;
    private String body;
    private UUID uuid;
}
