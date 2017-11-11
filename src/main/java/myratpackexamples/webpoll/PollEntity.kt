package myratpackexamples.webpoll

class PollEntity {
    data class Poll(
            val id: String?,
            val topic: String?,
            val options: List<String>,
            val votes: List<Vote>
    )

    data class Vote(
            val voter: String?,
            val selections: List<Selection>
    )

    data class Selection(
            val option: String,
            val selected: Selected
    )

    enum class Selected {
        YES, NO, MAYBE
    }
}