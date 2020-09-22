package laplacian.tutorial.entity.post

import laplacian.tutorial.api.query.*
import laplacian.tutorial.api.util.*

data class PostSearchInput (
    val id: IntSearchInput = IntSearchInput(),
    val userId: IntSearchInput = IntSearchInput(),
    val title: StringSearchInput = StringSearchInput(),
    val body: StringSearchInput = StringSearchInput()
) {
    fun isEmpty(): Boolean =
        id.isEmpty() &&
        userId.isEmpty() &&
        title.isEmpty() &&
        body.isEmpty()

    companion object {
        fun from(args: Map<String, Any?>): PostSearchInput {
            return PostSearchInput(
                id = IntSearchInput.from(args["id"]),
                userId = IntSearchInput.from(args["userId"]),
                title = StringSearchInput.from(args["title"]),
                body = StringSearchInput.from(args["body"])
            )
        }
    }
}