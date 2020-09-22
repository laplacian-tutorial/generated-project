package laplacian.tutorial.entity.comment
import java.util.concurrent.CompletableFuture
import laplacian.tutorial.entity.post.PostEntity
import laplacian.tutorial.entity.post.PostSearchInput


typealias Records = List<Record>
typealias Record = Map<String, Any?>

/**
 * The interface which abstracts the details of the data access concerning comments.
 */
interface CommentRepository {


    /**
     * Finds comments which matches the given conditions.
     */
    fun findComments(input: CommentSearchInput = CommentSearchInput()): CompletableFuture<Set<CommentEntity>>

    /**
     * Counts the number of comments which matches the given conditions.
     */
    fun countComments(input: CommentSearchInput = CommentSearchInput()): CompletableFuture<Long>

    /**
     * Loads comments having given primary keys.
     */
    fun loadComments(keys: Set<CommentEntity>): CompletableFuture<Set<CommentEntity>>

    /**
     * Loads post
     */
    fun loadPostOfComment(inputs: Map<PostSearchInput, Set<CommentEntity>>): CompletableFuture<Map<Pair<PostSearchInput, CommentEntity>, PostEntity>>

}