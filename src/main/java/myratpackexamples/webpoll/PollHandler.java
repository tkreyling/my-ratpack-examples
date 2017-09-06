package myratpackexamples.webpoll;

import ratpack.handling.Context;

import static ratpack.jackson.Jackson.fromJson;

public class PollHandler {
    public static void createPoll(Context context) {
        context.parse(fromJson(Poll.class))
                .then(poll -> {
                    if (poll.getTopic() == null || poll.getTopic().equals("")) {
                        context.getResponse().status(400).send("");
                    } else {
                        context.getResponse().status(201).send("");
                    }
                });
    }
}
