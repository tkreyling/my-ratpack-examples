package myratpackexamples.webpoll;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import ratpack.handling.Context;

import static ratpack.jackson.Jackson.fromJson;

public class PollHandler {
    public static void createPoll(Context context) {
        context.parse(fromJson(Poll.class))
                .then(poll -> {
                    if (poll.getTopic() == null || poll.getTopic().equals("")) {
                        context.getResponse().status(HttpResponseStatus.BAD_REQUEST.code());
                        context.getResponse().send("");
                    } else {
                        context.getResponse().getHeaders().add(HttpHeaderNames.LOCATION, "123");
                        context.getResponse().status(HttpResponseStatus.CREATED.code());
                        context.getResponse().send("");
                    }
                });
    }
}
