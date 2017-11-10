package myratpackexamples.webpoll

import myratpackexamples.webpoll.createpoll.CreatePollHandler
import myratpackexamples.webpoll.createvote.CreateVoteHandler
import ratpack.guice.Guice
import ratpack.handling.Chain
import ratpack.server.RatpackServer
import ratpack.server.RatpackServerSpec

private fun addHandlersToChain(chain: Chain): Chain = chain
        .post("poll", CreatePollHandler::class.java)
        .get("poll/:poll", RetrievePollHandler::class.java)
        .post("poll/:poll/vote", CreateVoteHandler::class.java)

fun setupServer(server: RatpackServerSpec): RatpackServerSpec = server
        .registry(Guice.registry { it.module(PollModule::class.java) })
        .handlers { addHandlersToChain(it) }

fun main(args: Array<String>) {
    InMemoryMongoDb()
    RatpackServer.start { setupServer(it) }
}