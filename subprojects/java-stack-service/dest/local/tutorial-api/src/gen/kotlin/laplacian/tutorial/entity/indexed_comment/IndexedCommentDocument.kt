package laplacian.tutorial.entity.indexed_comment

data class IndexedCommentDocument(
    val postId: Int,
    val seqNumber: Int,
    val name: String,
    val email: String,
    val body: String,
)