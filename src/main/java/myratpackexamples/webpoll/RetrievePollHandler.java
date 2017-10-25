package myratpackexamples.webpoll;

import com.google.inject.Inject;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.AllArgsConstructor;
import lombok.Value;
import ratpack.handling.Context;
import ratpack.handling.Handler;
import ratpack.jackson.Jackson;

@Value
@AllArgsConstructor(onConstructor=@__(@Inject))
public class RetrievePollHandler implements Handler {
    PollRepository pollRepository;

    @Override
    public void handle(Context context) throws Exception {
        String pollId = context.getPathTokens().get("poll");

        pollRepository.retrievePoll(pollId)
                .toEither()
                .peek(pollPromise -> pollPromise
                        .onError(error -> createNotFoundResponse(context))
                        .then(poll -> createSuccessResponse(context, poll))
                )
                .peekLeft(error -> createBadRequestResponse(context));
    }

    private static void createSuccessResponse(Context context, Poll poll) {
        context.getResponse().getHeaders().add(HttpHeaderNames.LOCATION, "poll/" + poll.getId());
        context.getResponse().status(HttpResponseStatus.OK.code());
        context.render(Jackson.json(poll));
    }

    private static void createBadRequestResponse(Context context) {
        context.getResponse().status(HttpResponseStatus.BAD_REQUEST.code());
        context.getResponse().send("");
    }

    private static void createNotFoundResponse(Context context) {
        context.getResponse().status(HttpResponseStatus.NOT_FOUND.code());
        context.getResponse().send("");
    }
}
