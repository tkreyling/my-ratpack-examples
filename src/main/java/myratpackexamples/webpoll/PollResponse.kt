package myratpackexamples.webpoll

class PollResponse {
    data class Poll(
            val id: String?,
            val topic: String?,
            val options: List<String>,
            val votes: List<Vote>
    ) {
        @Suppress("unused")
        private constructor() : this(null, null, emptyList(), emptyList())
    }

    data class Vote(
            val voter: String?,
            val selections: List<Selection>
    ) {
        @Suppress("unused")
        private constructor() : this(null, emptyList())
    }

    data class Selection(
            val option: String?,
            val selected: SelectedResponse?
    ) {
        @Suppress("unused")
        private constructor() : this(null, null)
    }

    enum class SelectedResponse {
        YES, NO, MAYBE
    }
}
