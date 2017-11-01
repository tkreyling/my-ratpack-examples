package myratpackexamples.webpoll

import com.google.inject.Inject
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpResponseStatus.*
import io.vavr.collection.Seq
import io.vavr.collection.List
import io.vavr.control.Validation
import io.vavr.control.Validation.*
import myratpackexamples.webpoll.Error.VoterMustBeNonEmpty
import myratpackexamples.webpoll.Error.InvalidValueForSelected
import myratpackexamples.webpoll.Error.UnknownOption
import myratpackexamples.webpoll.Error.TechnicalError
import myratpackexamples.webpoll.FindOneError.ExactlyOneElementExpected
import myratpackexamples.webpoll.FindOneError.InvalidIdString
import ratpack.exec.Promise
import ratpack.handling.Context
import ratpack.handling.Handler
import ratpack.jackson.Jackson

class CreateVoteHandler @Inject constructor(val pollRepository: PollRepository) : Handler {

    override fun handle(context: Context) {
        val pollId = context.pathTokens["poll"]
        val poll = retrievePoll(pollId)

        val voteRequest = context.parse(Jackson.fromJson(VoteRequest::class.java))

        poll.right(voteRequest).then { pair ->
            pair.left.flatMap { VoteRequestValidator(it).validateRequest(pair.right) }
                    .toEither()
                    .peek(context::createSuccessResponse)
                    .peekLeft(context::createErrorResponse)

        }
    }

    private fun retrievePoll(pollId: String?): Promise<Validation<Seq<Error>, Poll>> {
        return pollRepository.retrievePoll(pollId)
                .map { validation -> validation.mapError<Seq<Error>> { error -> List.of(TechnicalError(error)) } }
    }
}

class VoteRequestValidator(val poll: Poll) {
    fun validateRequest(voteRequest: VoteRequest): Validation<Seq<Error>, VoteRequestValidated> =
            Validation.combine(
                    validateVoter(voteRequest.voter).mapError<Seq<Error>> { List.of(it) },
                    validateSelections(voteRequest.selections)
            ).ap(::VoteRequestValidated)
                    .mapError { it.flatMap { inner -> inner } }

    private fun validateVoter(topic: String?): Validation<Error, String> =
            if (topic == null || topic == "") invalid(VoterMustBeNonEmpty) else valid(topic)

    private fun validateSelections(selections: kotlin.collections.List<Selection>?):
            Validation<Seq<Error>, kotlin.collections.List<SelectionValidated>> =
            Validation.sequence((selections ?: emptyList()).map { validateSelection(it) })
                    .map { it.asJava() }

    private fun validateSelection(it: Selection): Validation<Seq<Error>, SelectionValidated> {
        return combine(
                validateOption(it.option),
                validateSelected(it.selected)
        ).ap(::SelectionValidated)
    }

    private fun validateOption(option: String?): Validation<Error, String> {
        val optionNullSafe = option ?: ""
        return if (poll.options.contains(optionNullSafe))
            valid(optionNullSafe)
        else
            invalid(UnknownOption(option))
    }

    private fun validateSelected(selected: String?): Validation<Error, Selected> {
        return try  {
            valid(Selected.valueOf(selected?.toUpperCase() ?: "NO"))
        } catch (e: IllegalArgumentException) {
            invalid(InvalidValueForSelected(selected))
        }
    }
}

sealed class Error {
    data class TechnicalError(val findOneError: FindOneError) : Error()
    data class InvalidValueForSelected(val invalidValue: String?) : Error()
    data class UnknownOption(val unknownOption: String?) : Error()
    object VoterMustBeNonEmpty : Error()
}

private fun Context.createSuccessResponse(voteRequestValidated: VoteRequestValidated) {
    response.status(HttpResponseStatus.CREATED.code())
    response.send("")
}

private fun Context.createErrorResponse(errors: Seq<Error>) {
    errors.map { mapErrorToResponseCode(it).code() }.min().peek {
        response.status(it)
        response.send("")
    }
}

private fun mapErrorToResponseCode(error: Error): HttpResponseStatus {
    return when (error) {
        is VoterMustBeNonEmpty -> BAD_REQUEST
        is InvalidValueForSelected -> BAD_REQUEST
        is UnknownOption -> BAD_REQUEST
        is TechnicalError -> {
            when (error.findOneError) {
                is InvalidIdString -> BAD_REQUEST
                is ExactlyOneElementExpected -> NOT_FOUND
                else -> INTERNAL_SERVER_ERROR
            }
        }
    }
}