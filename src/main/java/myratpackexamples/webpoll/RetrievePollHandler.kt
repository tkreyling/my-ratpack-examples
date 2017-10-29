package myratpackexamples.webpoll

import com.google.inject.Inject
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpResponseStatus
import myratpackexamples.webpoll.RatpackMongoClient.FindOneError
import myratpackexamples.webpoll.RatpackMongoClient.FindOneError.ExactlyOneElementExpected
import myratpackexamples.webpoll.RatpackMongoClient.FindOneError.InvalidIdString
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

private fun Context.createSuccessResponse(poll: Poll) {
    response.headers.add(HttpHeaderNames.LOCATION, "poll/" + poll.id)
    response.status(HttpResponseStatus.OK.code())
    render(Jackson.json(poll))
}

private fun Context.createFailureResponse(error: FindOneError) {
    if (error is ExactlyOneElementExpected) {
        response.status(HttpResponseStatus.NOT_FOUND.code())
    } else if (error is InvalidIdString) {
        response.status(HttpResponseStatus.BAD_REQUEST.code())
    } else {
        response.status(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
    }
    response.send("")
}
