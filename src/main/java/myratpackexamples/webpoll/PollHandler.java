package myratpackexamples.webpoll;

import ratpack.handling.Context;

public class PollHandler {
    public static void createPoll(Context context) {
        context.getResponse().status(201).send("");
    }
}
