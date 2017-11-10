package myratpackexamples.webpoll.createpoll

data class PollRequestValidated(
        val topic: String,
        val options: List<String>
)