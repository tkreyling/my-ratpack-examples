package myratpackexamples.threads;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ratpack.exec.Blocking;
import ratpack.server.RatpackServer;

import java.util.UUID;

public class ThreadPools {
    private static Logger logger = LoggerFactory.getLogger(ThreadLocals.class);

    public static void main(String... args) throws Exception {
        logger.info("Starting ...");

        RatpackServer.start(server -> server
                .handlers(chain -> chain
                        .get(context -> {
                                    UUID requestId = UUID.randomUUID();
                                    logger.info(requestId + " | Start Request");

                                    Blocking.get(() -> {
                                        logger.info(requestId + " | Start Blocking Call");
                                        Thread.sleep(5000);
                                        logger.info(requestId + " | End Blocking Call");
                                        return requestId + " | Result of blocking call.";
                                    })
                                            .then(result -> {
                                                logger.info(requestId + " | Stop Request");
                                                context.render(result);
                                            });
                                }
                        )
                )
        );
    }
}
