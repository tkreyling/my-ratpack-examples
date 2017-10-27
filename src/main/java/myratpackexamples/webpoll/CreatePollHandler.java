package myratpackexamples.webpoll;

import com.google.inject.Inject;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.control.Validation;
import lombok.AllArgsConstructor;
import lombok.Value;
import myratpackexamples.promises.ValidationUtil;
import myratpackexamples.webpoll.RatpackMongoClient.InsertOneError;
import ratpack.exec.Promise;
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
                ValidationUtil.flatMapPromise(
                        mapRequestToDomainObject(pollRequest),
                        this::storePoll
                ).then(validation -> validation
                        .toEither()
                        .peek(poll -> createSuccessResponse(context, poll))
                        .peekLeft(errors -> createErrorResponse(context))
                )
        );
    }

    private Promise<Validation<Seq<Error>, Poll>> storePoll(PollRequest poll) {
        return pollRepository.storePoll(poll)
                .map(validation -> validation.mapError(error -> List.of(new TechnicalError(error))));
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

    private static Validation<Seq<Error>, PollRequest> mapRequestToDomainObject(PollRequest pollRequest) {
        return Validation.combine(
                createTopic(pollRequest.getTopic()),
                valid(pollRequest.getOptions())
        ).ap(PollRequest::new);
    }

    private static Validation<Error, String> createTopic(String topic) {
        if (topic == null || topic.equals("")) return invalid(new TopicMustBeNonEmpty());
        return valid(topic);
    }

    private abstract static class Error {
    }

    @Value
    private static class TechnicalError extends Error {
        InsertOneError insertOneError;
    }

    private static class TopicMustBeNonEmpty extends Error {
    }
}
