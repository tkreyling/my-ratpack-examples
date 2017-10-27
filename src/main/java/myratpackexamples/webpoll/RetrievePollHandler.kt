package myratpackexamples.webpoll

import com.google.inject.Inject
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpResponseStatus
import myratpackexamples.webpoll.RatpackMongoClient.*
import ratpack.handling.Context
import ratpack.handling.Handler
import ratpack.jackson.Jackson

class RetrievePollHandler @Inject constructor(val pollRepository: PollRepository) : Handler {

    override fun handle(context: Context) {
        val pollId = context.pathTokens["poll"]

        pollRepository.retrievePoll(pollId)
                .then { validation ->
                    validation
                            .toEither()
                            .peek { poll -> createSuccessResponse(context, poll) }
                            .peekLeft { error -> createFailureResponse(context, error) }
                }
    }

    private fun createSuccessResponse(context: Context, poll: Poll) {
        context.response.headers.add(HttpHeaderNames.LOCATION, "poll/" + poll.id)
        context.response.status(HttpResponseStatus.OK.code())
        context.render(Jackson.json(poll))
    }

    private fun createFailureResponse(context: Context, error: FindOneError) {
        if (error is ExactlyOneElementExpected) {
            context.response.status(HttpResponseStatus.NOT_FOUND.code())
        } else if (error is InvalidIdString) {
            context.response.status(HttpResponseStatus.BAD_REQUEST.code())
        } else {
            context.response.status(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
        }
        context.response.send("")
    }
}
