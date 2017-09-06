package myratpackexamples.webpoll;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import ratpack.handling.Context;

import static ratpack.jackson.Jackson.fromJson;

public class PollHandler {
    public static void createPoll(Context context) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new ParameterNamesModule());

        context.parse(fromJson(Poll.class, objectMapper))
                .then(poll -> {
                    if (poll.getTopic() == null || poll.getTopic().equals("")) {
                        context.getResponse().status(400).send("");
                    } else {
                        context.getResponse().status(201).send("");
                    }
                });
    }
}
