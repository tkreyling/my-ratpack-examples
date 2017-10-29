package myratpackexamples.webpoll;

import lombok.Value;

import java.util.List;

@Value
public class Poll {
    public String id;
    String topic;
    List<String> options;

    @SuppressWarnings("unused")
    private Poll() {
        this(null, null, null);
    }

    public Poll(String id, String topic, List<String> options) {
        this.id = id;
        this.topic = topic;
        this.options = options;
    }
}
