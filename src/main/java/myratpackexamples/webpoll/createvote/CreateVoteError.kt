package myratpackexamples.webpoll.createvote

import myratpackexamples.webpoll.FindOneError

sealed class CreateVoteError {
    data class TechnicalError(val findOneError: FindOneError) : CreateVoteError()
    data class InvalidValueForSelected(val invalidValue: String?) : CreateVoteError()
    data class UnknownOption(val unknownOption: String?) : CreateVoteError()
    object VoterMustBeNonEmpty : CreateVoteError()
}