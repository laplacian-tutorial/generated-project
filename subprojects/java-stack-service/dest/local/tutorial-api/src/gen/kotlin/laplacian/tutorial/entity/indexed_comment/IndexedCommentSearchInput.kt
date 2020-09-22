package laplacian.tutorial.entity.indexed_comment

import laplacian.tutorial.api.query.*
import laplacian.tutorial.api.util.*

data class IndexedCommentSearchInput (
    val postId: IntSearchInput = IntSearchInput(),
    val seqNumber: IntSearchInput = IntSearchInput(),
    val name: String = "",
    val email: String = "",
    val body: String = "",
) {
    fun isEmpty(): Boolean =
        postId.isEmpty() &&
        seqNumber.isEmpty() &&
        name.isEmpty() &&
        email.isEmpty() &&
        body.isEmpty()

    companion object {
        fun from(args: Map<String, Any?>): IndexedCommentSearchInput {
            return IndexedCommentSearchInput(
                postId = IntSearchInput.from(args["postId"]),
                seqNumber = IntSearchInput.from(args["seqNumber"]),
                name = args["name"] as? String ?: "",
                email = args["email"] as? String ?: "",
                body = args["body"] as? String ?: "",
            )
        }
    }
}