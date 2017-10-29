package myratpackexamples.webpoll

import com.google.inject.Inject
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpResponseStatus
import io.vavr.collection.Seq
import io.vavr.control.Validation
import io.vavr.control.Validation.invalid
import io.vavr.control.Validation.valid
import myratpackexamples.webpoll.Error.VoterMustBeNonEmpty
import ratpack.handling.Context
import ratpack.handling.Handler
import ratpack.jackson.Jackson

class CreateVoteHandler @Inject constructor(val pollRepository: PollRepository) : Handler {

    override fun handle(context: Context) {
        val pollId = context.pathTokens["poll"]
        pollRepository.retrievePoll(pollId)

        context.parse(Jackson.fromJson(VoteRequest::class.java)).then { voteRequest ->
            validateRequest(voteRequest).toEither()
                    .peek(context::createSuccessResponse)
                    .peekLeft(context::createErrorResponse)
        }
    }

    private fun validateRequest(voteRequest: VoteRequest): Validation<Seq<Error>, VoteRequestValidated> =
            Validation.combine(
                    validateVoter(voteRequest.voter),
                    validateSelections(voteRequest.selections)
            ).ap(::VoteRequestValidated)

    private fun validateVoter(topic: String?): Validation<Error, String> =
            if (topic == null || topic == "") invalid(VoterMustBeNonEmpty) else valid(topic)

    private fun validateSelections(selections: List<Selection>?): Validation<Error, List<SelectionValidated>> =
            valid(
                    (selections ?: emptyList())
                    .map { SelectionValidated(
                            it.option ?: "",
                            Selected.valueOf(it.selected?.toUpperCase() ?: "NO")
                    ) }
            )
}

sealed class Error {
    object VoterMustBeNonEmpty : Error()
}

private fun Context.createSuccessResponse(voteRequestValidated: VoteRequestValidated) {
    response.status(HttpResponseStatus.CREATED.code())
    response.send("")
}

private fun Context.createErrorResponse(errors: Seq<Error>) {
    response.status(HttpResponseStatus.BAD_REQUEST.code())
    response.send("")
}