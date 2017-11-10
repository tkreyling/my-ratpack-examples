package myratpackexamples.webpoll.createpoll

import myratpackexamples.webpoll.InsertOneError

sealed class CreatePollError {
    data class TechnicalError(val insertOneError: InsertOneError) : CreatePollError()
    object TopicMustBeNonEmpty : CreatePollError()
}