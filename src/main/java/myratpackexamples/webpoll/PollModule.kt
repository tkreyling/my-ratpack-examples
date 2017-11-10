package myratpackexamples.webpoll

import com.google.inject.Binder
import com.google.inject.Module
import myratpackexamples.webpoll.createvote.CreateVoteHandler

class PollModule : Module {
    override fun configure(binder: Binder) {
        binder.bind(CreatePollHandler::class.java)
        binder.bind(RetrievePollHandler::class.java)
        binder.bind(CreateVoteHandler::class.java)
    }
}
