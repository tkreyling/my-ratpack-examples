package myratpackexamples.webpoll

class VoteRequestValidated {
    data class Vote(
            val voter: String,
            val selections: List<Selection>
    )

    data class Selection(
            val option: String,
            val selected: Selected
    )
}

enum class Selected {
    YES, NO, MAYBE
}