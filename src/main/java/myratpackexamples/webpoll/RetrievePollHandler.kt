package myratpackexamples.webpoll

import com.google.inject.Inject
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpResponseStatus.*
import myratpackexamples.webpoll.FindOneError.ExactlyOneElementExpected
import myratpackexamples.webpoll.FindOneError.InvalidIdString
import ratpack.handling.Context
import ratpack.handling.Handler
import ratpack.jackson.Jackson

class RetrievePollHandler @Inject constructor(val pollRepository: PollRepository) : Handler {

    override fun handle(context: Context) {
        val pollId = context.pathTokens["poll"]

        pollRepository.retrievePoll(pollId)
                .then {
                    it.toEither()
                            .peek(context::createSuccessResponse)
                            .peekLeft(context::createFailureResponse)
                }
    }
}

private fun Context.createSuccessResponse(poll: PollResponse.Poll) {
    response.headers.add(HttpHeaderNames.LOCATION, "poll/" + poll.id)
    response.status(HttpResponseStatus.OK.code())
    render(Jackson.json(poll))
}

private fun Context.createFailureResponse(error: FindOneError) {
    response.status(mapErrorToResponseCode(error).code())
    response.send("")
}

private fun mapErrorToResponseCode(error: FindOneError): HttpResponseStatus {
    return when (error) {
        is ExactlyOneElementExpected -> NOT_FOUND
        is InvalidIdString -> BAD_REQUEST
        else -> INTERNAL_SERVER_ERROR
    }
}
