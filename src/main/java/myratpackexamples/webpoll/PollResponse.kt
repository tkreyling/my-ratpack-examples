package myratpackexamples.webpoll

class PollResponse {
    data class Poll(
            val id: String?,
            val topic: String?,
            val options: List<String>
    ) {
        @Suppress("unused")
        private constructor() : this(null, null, emptyList())
    }
}
