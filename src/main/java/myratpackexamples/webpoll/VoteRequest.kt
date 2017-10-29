package myratpackexamples.webpoll

data class VoteRequest(
        val voter: String?,
        val selections: List<Selection>?
) {
    @Suppress("unused")
    private constructor() : this(null, null)
}

data class Selection(
        val option: String?,
        val selected: String?
) {
    @Suppress("unused")
    private constructor() : this(null, null)
}