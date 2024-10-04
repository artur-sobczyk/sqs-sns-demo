package pl.artsobcz.demo.sqs;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Service
public class DemoMessageRepository {

    private final HashMap<String, DemoMessage> messages = new HashMap<>();

    void save(DemoMessage message) {
        messages.put(message.getKey(), message);
    }

    List<DemoMessage> getAll() {
        return messages.values().stream().toList();
    }

    void clear() {
        messages.clear();
    }
}
