package myratpackexamples.jsonparser

data class Person(
        val firstname: String?,
        val lastname: String?
) {
    @Suppress("unused")
    private constructor() : this(null, null)
}
