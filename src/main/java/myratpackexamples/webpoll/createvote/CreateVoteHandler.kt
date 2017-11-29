package myratpackexamples.webpoll.createvote

import com.google.inject.Inject
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpResponseStatus.*
import io.vavr.collection.Seq
import io.vavr.collection.List
import io.vavr.control.Validation
import io.vavr.control.Validation.*
import myratpackexamples.webpoll.*
import myratpackexamples.webpoll.createvote.CreateVoteError.VoterMustBeNonEmpty
import myratpackexamples.webpoll.createvote.CreateVoteError.InvalidValueForSelected
import myratpackexamples.webpoll.createvote.CreateVoteError.UnknownOption
import myratpackexamples.webpoll.createvote.CreateVoteError.TechnicalError
import myratpackexamples.webpoll.createvote.CreateVoteError.UpdatingPollWithVoteFailed
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

        val voteRequest = context.parse(Jackson.fromJson(VoteRequest.Vote::class.java))

        poll.right(voteRequest).flatMap { pair ->
            pair.left.flatMap { poll ->
                VoteRequestValidator(poll).validateRequest(pair.right)
                        .map { addVoteToPoll(poll, it) }
            }
                    .flatMapPromise { replacePoll(pollId, it) }
        }.then {
            it.toEither().peek(context::createSuccessResponse).peekLeft(context::createErrorResponse)
        }
    }

    private fun addVoteToPoll(poll: PollResponse.Poll, newVote: VoteRequestValidated.Vote): PollEntity.Poll {
        return PollEntity.Poll(
                id = poll.id,
                topic = poll.topic,
                options = poll.options,
                votes = poll.votes.map { vote ->
                    PollEntity.Vote(vote.voter, vote.selections.map {
                        PollEntity.Selection(it.option!!, PollEntity.Selected.valueOf(it.selected!!.name))
                    })
                }.plus(PollEntity.Vote(newVote.voter, newVote.selections.map {
                    PollEntity.Selection(it.option, PollEntity.Selected.valueOf(it.selected.name))
                }))
        )
    }

    private fun retrievePoll(pollId: String?): Promise<Validation<Seq<CreateVoteError>, PollResponse.Poll>> {
        return pollRepository.retrievePoll(pollId)
                .map { validation -> validation.mapError<Seq<CreateVoteError>> { error -> List.of(TechnicalError(error)) } }
    }

    private fun replacePoll(pollId: String?, poll: PollEntity.Poll): Promise<Validation<Seq<CreateVoteError>, PollResponse.Poll>> {
        return pollRepository.replacePoll(pollId, poll)
                .map { validation -> validation.mapError<Seq<CreateVoteError>> { error -> List.of(UpdatingPollWithVoteFailed(error)) } }
    }
}

class VoteRequestValidator(val poll: PollResponse.Poll) {
    fun validateRequest(voteRequest: VoteRequest.Vote): Validation<Seq<CreateVoteError>, VoteRequestValidated.Vote> =
            Validation.combine(
                    validateVoter(voteRequest.voter).mapError<Seq<CreateVoteError>> { List.of(it) },
                    validateSelections(voteRequest.selections)
            ).ap { voter, selections -> VoteRequestValidated.Vote(voter, selections) }
                    .mapError { it.flatMap { inner -> inner } }

    private fun validateVoter(topic: String?): Validation<CreateVoteError, String> =
            if (topic == null || topic == "") invalid(VoterMustBeNonEmpty) else valid(topic)

    private fun validateSelections(selections: kotlin.collections.List<VoteRequest.Selection>?):
            Validation<Seq<CreateVoteError>, kotlin.collections.List<VoteRequestValidated.Selection>> =
            Validation.sequence((selections ?: emptyList()).map { validateSelection(it) })
                    .map { it.asJava() }

    private fun validateSelection(it: VoteRequest.Selection): Validation<Seq<CreateVoteError>, VoteRequestValidated.Selection> {
        return combine(
                validateOption(it.option),
                validateSelected(it.selected)
        ).ap { option, selected -> VoteRequestValidated.Selection(option, selected) }
    }

    private fun validateOption(option: String?): Validation<CreateVoteError, String> {
        val optionNullSafe = option ?: ""
        return if (poll.options.contains(optionNullSafe))
            valid(optionNullSafe)
        else
            invalid(UnknownOption(option))
    }

    private fun validateSelected(selected: String?): Validation<CreateVoteError, VoteRequestValidated.Selected> {
        return try  {
            valid(VoteRequestValidated.Selected.valueOf(selected?.toUpperCase() ?: "NO"))
        } catch (e: IllegalArgumentException) {
            invalid(InvalidValueForSelected(selected))
        }
    }
}

private fun Context.createSuccessResponse(
        @Suppress("UNUSED_PARAMETER") poll: PollResponse.Poll
) {
    response.status(HttpResponseStatus.CREATED.code())
    response.send("")
}

private fun Context.createErrorResponse(errors: Seq<CreateVoteError>) {
    errors.map { mapErrorToResponseCode(it).code() }.min().peek {
        response.status(it)
        response.send("")
    }
}

private fun mapErrorToResponseCode(error: CreateVoteError): HttpResponseStatus {
    return when (error) {
        is VoterMustBeNonEmpty -> BAD_REQUEST
        is InvalidValueForSelected -> BAD_REQUEST
        is UnknownOption -> BAD_REQUEST
        is UpdatingPollWithVoteFailed -> INTERNAL_SERVER_ERROR
        is TechnicalError -> {
            when (error.findOneError) {
                is InvalidIdString -> BAD_REQUEST
                is ExactlyOneElementExpected -> NOT_FOUND
                else -> INTERNAL_SERVER_ERROR
            }
        }
    }
}