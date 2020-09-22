package laplacian.tutorial.entity.comment

import laplacian.tutorial.api.query.*
import laplacian.tutorial.api.util.*

data class CommentSearchInput (
    val postId: IntSearchInput = IntSearchInput(),
    val seqNumber: IntSearchInput = IntSearchInput(),
    val name: StringSearchInput = StringSearchInput(),
    val email: StringSearchInput = StringSearchInput(),
    val body: StringSearchInput = StringSearchInput()
) {
    fun isEmpty(): Boolean =
        postId.isEmpty() &&
        seqNumber.isEmpty() &&
        name.isEmpty() &&
        email.isEmpty() &&
        body.isEmpty()

    companion object {
        fun from(args: Map<String, Any?>): CommentSearchInput {
            return CommentSearchInput(
                postId = IntSearchInput.from(args["postId"]),
                seqNumber = IntSearchInput.from(args["seqNumber"]),
                name = StringSearchInput.from(args["name"]),
                email = StringSearchInput.from(args["email"]),
                body = StringSearchInput.from(args["body"])
            )
        }
    }
}