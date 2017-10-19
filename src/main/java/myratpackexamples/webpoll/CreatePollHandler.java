package myratpackexamples.webpoll;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vavr.collection.Seq;
import io.vavr.control.Validation;
import ratpack.handling.Context;
import ratpack.handling.Handler;

import java.util.UUID;

import static io.vavr.control.Validation.invalid;
import static io.vavr.control.Validation.valid;
import static ratpack.jackson.Jackson.fromJson;

public class CreatePollHandler implements Handler {
    @Override
    public void handle(Context context) throws Exception {
        context.parse(fromJson(PollRequest.class)).then(pollRequest ->
                mapRequestToDomainObject(pollRequest)
                        .peek(PollRepository::storePoll)
                        .toEither()
                        .peek(poll -> createSuccessResponse(context, poll))
                        .peekLeft(errors -> createErrorResponse(context))
        );
    }

    private static void createSuccessResponse(Context context, Poll poll) {
        context.getResponse().getHeaders().add(HttpHeaderNames.LOCATION, "poll/" + poll.getId());
        context.getResponse().status(HttpResponseStatus.CREATED.code());
        context.getResponse().send("");
    }

    private static void createErrorResponse(Context context) {
        context.getResponse().status(HttpResponseStatus.BAD_REQUEST.code());
        context.getResponse().send("");
    }

    private static Validation<Seq<String>, Poll> mapRequestToDomainObject(PollRequest pollRequest) {
        String pollId = UUID.randomUUID().toString();
        return Validation.combine(
                valid(pollId),
                createTopic(pollRequest.getTopic()),
                valid(pollRequest.getOptions())
        ).ap(Poll::new);
    }

    private static Validation<String, String> createTopic(String topic) {
        if (topic == null || topic.equals("")) return invalid("Topic must be non empty!");
        return valid(topic);
    }
}
