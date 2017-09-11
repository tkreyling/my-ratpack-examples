package myratpackexamples.webpoll;

import ratpack.handling.Context;
import ratpack.handling.Handler;

import static ratpack.jackson.Jackson.json;

public class RetrievePollHandler implements Handler {
    @Override
    public void handle(Context context) throws Exception {
        String pollId = context.getPathTokens().get("poll");
        context.render(json(PollRepository.retrievePoll(pollId)));
    }
}
