package myratpackexamples.webpoll;

import lombok.Value;

import java.util.List;

@Value
public class PollRequest {
    String topic;
    List<String> options;
}
