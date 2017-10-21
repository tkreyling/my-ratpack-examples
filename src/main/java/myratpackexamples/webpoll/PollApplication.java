package myratpackexamples.webpoll;

import ratpack.guice.Guice;
import ratpack.handling.Chain;
import ratpack.server.RatpackServer;
import ratpack.server.RatpackServerSpec;

public class PollApplication {
    private static Chain addHandlersToChain(Chain chain) {
        return chain
                .post("poll", CreatePollHandler.class)
                .get("poll/:poll", RetrievePollHandler.class);
    }

    public static RatpackServerSpec setupServer(RatpackServerSpec server) {
        return server
                .registry(Guice.registry(b -> b.module(PollModule.class)))
                .handlers(PollApplication::addHandlersToChain);
    }

    public static void main(String... args) throws Exception {
        new InMemoryMongoDb();
        RatpackServer.start(PollApplication::setupServer);
    }
}
