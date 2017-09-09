package myratpackexamples.webpoll;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import ratpack.handling.Chain;
import ratpack.handling.Context;
import ratpack.server.RatpackServer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static ratpack.jackson.Jackson.fromJson;
import static ratpack.jackson.Jackson.json;

public class PollHandler {
    private static Map<String, Poll> polls = new HashMap<>();

    public static void createPoll(Context context) {
        context.parse(fromJson(Poll.class))
                .then(poll -> {
                    if (poll.getTopic() == null || poll.getTopic().equals("")) {
                        context.getResponse().status(HttpResponseStatus.BAD_REQUEST.code());
                        context.getResponse().send("");
                    } else {
                        String pollId = UUID.randomUUID().toString();

                        polls.put(pollId, poll);

                        context.getResponse().getHeaders().add(HttpHeaderNames.LOCATION, "poll/" + pollId);
                        context.getResponse().status(HttpResponseStatus.CREATED.code());
                        context.getResponse().send("");
                    }
                });
    }

    public static void retrievePoll(Context context) {
        String pollId = context.getPathTokens().get("poll");
        context.render(json(polls.get(pollId)));
    }

    public static Chain pollHandlerChain(Chain chain) {
        return chain
                .post("poll", PollHandler::createPoll)
                .get("poll/:poll", PollHandler::retrievePoll);
    }

    public static void main(String... args) throws Exception {
        RatpackServer.start(server -> server.handlers(PollHandler::pollHandlerChain));
    }
}
