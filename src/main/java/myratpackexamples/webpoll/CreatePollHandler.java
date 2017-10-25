package myratpackexamples.webpoll;

import com.google.inject.Inject;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vavr.collection.Seq;
import io.vavr.control.Validation;
import lombok.AllArgsConstructor;
import lombok.Value;
import ratpack.handling.Context;
import ratpack.handling.Handler;

import static io.vavr.control.Validation.invalid;
import static io.vavr.control.Validation.valid;
import static ratpack.jackson.Jackson.fromJson;

@Value
@AllArgsConstructor(onConstructor=@__(@Inject))
public class CreatePollHandler implements Handler {
    PollRepository pollRepository;

    @Override
    public void handle(Context context) throws Exception {
        context.parse(fromJson(PollRequest.class)).then(pollRequest ->
                mapRequestToDomainObject(pollRequest)
                        .map(pollRepository::storePoll)
                        .toEither()
                        .peek(pollPromise -> pollPromise
                                .onError(throwable -> createErrorResponse(context))
                                .then(poll -> createSuccessResponse(context, poll))
                        )
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

    private static Validation<Seq<String>, PollRequest> mapRequestToDomainObject(PollRequest pollRequest) {
        return Validation.combine(
                createTopic(pollRequest.getTopic()),
                valid(pollRequest.getOptions())
        ).ap(PollRequest::new);
    }

    private static Validation<String, String> createTopic(String topic) {
        if (topic == null || topic.equals("")) return invalid("Topic must be non empty!");
        return valid(topic);
    }
}
