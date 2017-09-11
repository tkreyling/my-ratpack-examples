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
}
