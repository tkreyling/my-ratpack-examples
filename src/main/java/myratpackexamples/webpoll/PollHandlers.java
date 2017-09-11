package myratpackexamples.webpoll;

import ratpack.handling.Chain;
import ratpack.server.RatpackServer;

public class PollHandlers {
    public static Chain addToChain(Chain chain) {
        return chain
                .post("poll", new CreatePollHandler())
                .get("poll/:poll", new RetrievePollHandler());
    }

    public static void main(String... args) throws Exception {
        RatpackServer.start(server -> server.handlers(PollHandlers::addToChain));
    }
}
