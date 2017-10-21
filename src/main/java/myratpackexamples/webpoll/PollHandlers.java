package myratpackexamples.webpoll;

import ratpack.guice.Guice;
import ratpack.handling.Chain;
import ratpack.server.RatpackServer;
import ratpack.server.RatpackServerSpec;

public class PollHandlers {
    private static Chain addToChain(Chain chain) {
        return chain
                .post("poll", CreatePollHandler.class)
                .get("poll/:poll", RetrievePollHandler.class);
    }

    public static RatpackServerSpec setupServer(RatpackServerSpec server) {
        return server
                .registry(Guice.registry(b -> b.module(PollModule.class)))
                .handlers(PollHandlers::addToChain);
    }

    public static void main(String... args) throws Exception {
        RatpackServer.start(PollHandlers::setupServer);
    }
}
