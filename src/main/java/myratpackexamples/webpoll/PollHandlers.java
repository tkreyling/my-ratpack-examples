package myratpackexamples.webpoll;

import ratpack.handling.Chain;

public class PollHandlers {
    public static Chain addToChain(Chain chain) {
        return chain
                .post("poll", PollHandler::createPoll)
                .get("poll/:poll", PollHandler::retrievePoll);
    }
}
