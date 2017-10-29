package myratpackexamples.webpoll

import ratpack.guice.Guice
import ratpack.handling.Chain
import ratpack.server.RatpackServer
import ratpack.server.RatpackServerSpec

object PollApplication {
    private fun addHandlersToChain(chain: Chain): Chain {
        return chain
                .post("poll", CreatePollHandler::class.java)
                .get("poll/:poll", RetrievePollHandler::class.java)
    }

    fun setupServer(server: RatpackServerSpec): RatpackServerSpec {
        return server
                .registry(Guice.registry { b -> b.module(PollModule::class.java) })
                .handlers { addHandlersToChain(it) }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        InMemoryMongoDb()
        RatpackServer.start{ setupServer(it) }
    }
}
