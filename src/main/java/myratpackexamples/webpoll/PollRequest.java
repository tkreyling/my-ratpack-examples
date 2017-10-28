package myratpackexamples.webpoll;

import lombok.Value;

import java.util.List;

@Value
public class PollRequest {
    public String topic;
    public List<String> options;

    @SuppressWarnings("unused")
    private PollRequest() {
        this(null, null);
    }

    public PollRequest(String topic, List<String> options) {
        this.topic = topic;
        this.options = options;
    }
}
