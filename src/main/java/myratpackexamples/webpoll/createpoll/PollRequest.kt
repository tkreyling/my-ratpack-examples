package myratpackexamples.webpoll.createpoll

data class PollRequest(
        val topic: String?,
        val options: List<String>?
) {
    @Suppress("unused")
    private constructor() : this(null, null)
}
