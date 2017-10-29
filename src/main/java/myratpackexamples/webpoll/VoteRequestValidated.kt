package myratpackexamples.webpoll

data class VoteRequestValidated(
        val voter: String,
        val selections: List<SelectionValidated>
)

data class SelectionValidated(
        val option: String,
        val selected: Selected
)

enum class Selected {
    YES, NO, MAYBE
}