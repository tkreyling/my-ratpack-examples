package myratpackexamples.threads;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ratpack.exec.Blocking;
import ratpack.server.RatpackServer;

import java.util.UUID;

public class ThreadLocals {
    private static Logger logger = LoggerFactory.getLogger(ThreadLocals.class);

    public static void main(String... args) throws Exception {
        logger.info("Starting ...");
        ThreadLocal<UUID> badIdea = new ThreadLocal<>();

        RatpackServer.start(server -> server
                .handlers(chain -> chain
                        .get(context -> {
                                    UUID requestId = UUID.randomUUID();
                                    badIdea.set(requestId);
                                    logger.info(requestId + " | " + badIdea.get() + " | Start Request");

                                    Blocking.get(() -> {
                                        logger.info(requestId + " | " + badIdea.get() + " | Start Blocking Call");
                                        Thread.sleep(5000);
                                        logger.info(requestId + " | " + badIdea.get() + " | End Blocking Call");
                                        return requestId + " | " + badIdea.get() + " | Result of blocking call.";
                                    })
                                            .then(result -> {
                                                logger.info(requestId + " | " + badIdea.get() + " | Stop Request");
                                                context.render(result);
                                            });
                                }
                        )
                )
        );
    }
}
