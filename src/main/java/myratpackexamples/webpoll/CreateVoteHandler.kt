package myratpackexamples.webpoll

import com.google.inject.Inject
import io.netty.handler.codec.http.HttpResponseStatus
import ratpack.handling.Context
import ratpack.handling.Handler

class CreateVoteHandler @Inject constructor(val pollRepository: PollRepository) : Handler {

    override fun handle(ctx: Context) {
        ctx.createErrorResponse()
    }
}

private fun Context.createErrorResponse() {
    response.status(HttpResponseStatus.BAD_REQUEST.code())
    response.send("")
}