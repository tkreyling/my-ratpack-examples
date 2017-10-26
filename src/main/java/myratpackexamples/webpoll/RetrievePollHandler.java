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
                .then(validation -> validation
                        .toEither()
                        .peek(poll -> createSuccessResponse(context, poll))
                        .peekLeft(error -> createFailureResponse(context, error))
                );
    }

    private static void createSuccessResponse(Context context, Poll poll) {
        context.getResponse().getHeaders().add(HttpHeaderNames.LOCATION, "poll/" + poll.getId());
        context.getResponse().status(HttpResponseStatus.OK.code());
        context.render(Jackson.json(poll));
    }

    private static void createFailureResponse(Context context, String error) {
        if (error.equals("RatpackMongoClient.ExactlyOneElementExpected()")) {
            context.getResponse().status(HttpResponseStatus.NOT_FOUND.code());
        } else if (error.equals("Non valid id!")) {
            context.getResponse().status(HttpResponseStatus.BAD_REQUEST.code());
        } else {
            context.getResponse().status(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
        }
        context.getResponse().send("");
    }
}
