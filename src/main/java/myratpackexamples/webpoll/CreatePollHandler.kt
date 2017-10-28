package myratpackexamples.webpoll

import com.google.inject.Inject
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpResponseStatus
import io.vavr.collection.List
import io.vavr.collection.Seq
import io.vavr.control.Validation
import io.vavr.control.Validation.invalid
import io.vavr.control.Validation.valid
import myratpackexamples.promises.ValidationUtil
import myratpackexamples.webpoll.CreatePollHandler.Error.TechnicalError
import myratpackexamples.webpoll.CreatePollHandler.Error.TopicMustBeNonEmpty
import myratpackexamples.webpoll.RatpackMongoClient.InsertOneError
import ratpack.exec.Promise
import ratpack.handling.Context
import ratpack.handling.Handler
import ratpack.jackson.Jackson.fromJson
import java.util.function.Function

class CreatePollHandler @Inject constructor(val pollRepository: PollRepository) : Handler {

    override fun handle(context: Context) {
        context.parse(fromJson(PollRequest::class.java)).then { pollRequest ->
            mapRequestToDomainObject(pollRequest)
                    .flatMapPromise(this::storePoll)
                    .then { validation ->
                        validation
                                .toEither()
                                .peek { poll -> createSuccessResponse(context, poll) }
                                .peekLeft { errors -> createErrorResponse(context, errors) }
                    }
        }
    }

    private fun storePoll(poll: PollRequest): Promise<Validation<Seq<Error>, Poll>> {
        return pollRepository.storePoll(poll)
                .map { validation -> validation.mapError<Seq<Error>> { error -> List.of(TechnicalError(error)) } }
    }

    private fun createSuccessResponse(context: Context, poll: Poll) {
        context.response.headers.add(HttpHeaderNames.LOCATION, "poll/" + poll.id)
        context.response.status(HttpResponseStatus.CREATED.code())
        context.response.send("")
    }

    private fun createErrorResponse(context: Context, errors: Seq<Error>) {
        errors.filter { error -> error is TechnicalError }
                .forEach { _ -> context.response.status(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()) }
        errors.filter { error -> error is TopicMustBeNonEmpty }
                .forEach { _ -> context.response.status(HttpResponseStatus.BAD_REQUEST.code()) }

        context.response.send("")
    }

    private fun mapRequestToDomainObject(pollRequest: PollRequest): Validation<Seq<Error>, PollRequest> =
            Validation.combine(
                    createTopic(pollRequest.topic),
                    valid(pollRequest.options)
            ).ap(::PollRequest)

    private fun createTopic(topic: String?): Validation<Error, String> =
            if (topic == null || topic == "") invalid(TopicMustBeNonEmpty) else valid(topic)

    sealed class Error {
        data class TechnicalError( val insertOneError: InsertOneError) : Error()
        object TopicMustBeNonEmpty : Error()
    }
}

fun <E, I, O> Validation<E, I>.flatMapPromise(function: (I) -> Promise<Validation<E, O>>): Promise<Validation<E, O>> =
        ValidationUtil.flatMapPromise(this, object : Function<I, Promise<Validation<E, O>>> {
            override fun apply(i: I): Promise<Validation<E, O>> = function.invoke(i)
        })
