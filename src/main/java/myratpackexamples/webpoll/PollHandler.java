package myratpackexamples.webpoll;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import ratpack.handling.Context;
import ratpack.server.RatpackServer;

import java.util.UUID;

import static ratpack.jackson.Jackson.fromJson;
import static ratpack.jackson.Jackson.json;

public class PollHandler {

    public static void createPoll(Context context) {
        context.parse(fromJson(Poll.class))
                .then(poll -> {
                    if (poll.getTopic() == null || poll.getTopic().equals("")) {
                        context.getResponse().status(HttpResponseStatus.BAD_REQUEST.code());
                        context.getResponse().send("");
                    } else {
                        String pollId = UUID.randomUUID().toString();

                        PollRepository.storePoll(poll, pollId);

                        context.getResponse().getHeaders().add(HttpHeaderNames.LOCATION, "poll/" + pollId);
                        context.getResponse().status(HttpResponseStatus.CREATED.code());
                        context.getResponse().send("");
                    }
                });
    }

    public static void retrievePoll(Context context) {
        String pollId = context.getPathTokens().get("poll");
        context.render(json(PollRepository.retrievePoll(pollId)));
    }

    public static void main(String... args) throws Exception {
        RatpackServer.start(server -> server.handlers(PollHandlers::addToChain));
    }
}
