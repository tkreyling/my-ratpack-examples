package myratpackexamples.webpoll.createpoll

import com.google.inject.Inject
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST
import io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR
import io.vavr.collection.List
import io.vavr.collection.Seq
import io.vavr.control.Validation
import io.vavr.control.Validation.invalid
import io.vavr.control.Validation.valid
import myratpackexamples.webpoll.*
import myratpackexamples.webpoll.createpoll.CreatePollError.TechnicalError
import myratpackexamples.webpoll.createpoll.CreatePollError.TopicMustBeNonEmpty
import ratpack.exec.Promise
import ratpack.handling.Context
import ratpack.handling.Handler
import ratpack.jackson.Jackson.fromJson

class CreatePollHandler @Inject constructor(val pollRepository: PollRepository) : Handler {

    override fun handle(context: Context) {
        context.parse(fromJson(PollRequest::class.java)).then { pollRequest ->
            validateRequest(pollRequest)
                    .flatMapPromise(this::storePoll)
                    .then {
                        it.toEither()
                                .peek(context::createSuccessResponse)
                                .peekLeft(context::createErrorResponse)
                    }
        }
    }

    private fun storePoll(poll: PollRequestValidated): Promise<Validation<Seq<CreatePollError>, PollResponse.Poll>> {
        return pollRepository.storePoll(poll)
                .map { validation -> validation.mapError<Seq<CreatePollError>> { error -> List.of(TechnicalError(error)) } }
    }

    private fun validateRequest(pollRequest: PollRequest): Validation<Seq<CreatePollError>, PollRequestValidated> =
            Validation.combine(
                    validateTopic(pollRequest.topic),
                    valid(pollRequest.options ?: emptyList())
            ).ap(::PollRequestValidated)

    private fun validateTopic(topic: String?): Validation<CreatePollError, String> =
            if (topic == null || topic == "") invalid(TopicMustBeNonEmpty) else valid(topic)

}

private fun Context.createSuccessResponse(poll: PollResponse.Poll) {
    response.headers.add(HttpHeaderNames.LOCATION, "poll/" + poll.id)
    response.status(HttpResponseStatus.CREATED.code())
    response.send("")
}

private fun Context.createErrorResponse(errors: Seq<CreatePollError>) {
    errors.map { mapErrorToResponseCode(it).code() }.min().peek {
        response.status(it)
        response.send("")
    }
}

private fun mapErrorToResponseCode(error: CreatePollError): HttpResponseStatus {
    return when (error) {
        is TopicMustBeNonEmpty -> BAD_REQUEST
        is TechnicalError -> INTERNAL_SERVER_ERROR
    }
}