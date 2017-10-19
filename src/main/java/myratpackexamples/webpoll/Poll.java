package myratpackexamples.webpoll;

import lombok.Value;

import java.util.List;

@Value
public class Poll {
    String id;
    String topic;
    List<String> options;
}
