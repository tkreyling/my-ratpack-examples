package myratpackexamples.webpoll

data class PollRequestValidated(
        val topic: String,
        val options: List<String>
)