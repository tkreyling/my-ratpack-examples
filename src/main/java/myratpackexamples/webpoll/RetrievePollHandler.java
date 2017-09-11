package myratpackexamples.webpoll;

import ratpack.handling.Context;

import static ratpack.jackson.Jackson.json;

public class RetrievePollHandler {
    public static void retrievePoll(Context context) {
        String pollId = context.getPathTokens().get("poll");
        context.render(json(PollRepository.retrievePoll(pollId)));
    }
}
