package myratpackexamples.webpoll

import ratpack.guice.Guice
import ratpack.handling.Chain
import ratpack.server.RatpackServer
import ratpack.server.RatpackServerSpec

private fun addHandlersToChain(chain: Chain): Chain = chain
        .post("poll", CreatePollHandler::class.java)
        .get("poll/:poll", RetrievePollHandler::class.java)

fun setupServer(server: RatpackServerSpec): RatpackServerSpec = server
        .registry(Guice.registry { it.module(PollModule::class.java) })
        .handlers { addHandlersToChain(it) }

fun main(args: Array<String>) {
    InMemoryMongoDb()
    RatpackServer.start { setupServer(it) }
}