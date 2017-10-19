package myratpackexamples.webpoll;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import ratpack.handling.Context;
import ratpack.handling.Handler;

import java.util.UUID;

import static ratpack.jackson.Jackson.fromJson;

public class CreatePollHandler implements Handler {
    @Override
    public void handle(Context context) throws Exception {
        context.parse(fromJson(PollRequest.class))
                .then(pollRequest -> {
                    if (pollRequest.getTopic() == null || pollRequest.getTopic().equals("")) {
                        context.getResponse().status(HttpResponseStatus.BAD_REQUEST.code());
                        context.getResponse().send("");
                    } else {
                        String pollId = UUID.randomUUID().toString();

                        Poll poll = new Poll(pollId, pollRequest.getTopic(), pollRequest.getOptions());
                        PollRepository.storePoll(poll);

                        context.getResponse().getHeaders().add(HttpHeaderNames.LOCATION, "poll/" + pollId);
                        context.getResponse().status(HttpResponseStatus.CREATED.code());
                        context.getResponse().send("");
                    }
                });
    }
}
