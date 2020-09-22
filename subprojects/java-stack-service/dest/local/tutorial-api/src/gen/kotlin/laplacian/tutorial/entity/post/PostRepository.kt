package laplacian.tutorial.entity.post
import java.util.concurrent.CompletableFuture
import laplacian.tutorial.entity.user.UserEntity
import laplacian.tutorial.entity.user.UserSearchInput
import laplacian.tutorial.entity.comment.CommentEntity
import laplacian.tutorial.entity.comment.CommentSearchInput


typealias Records = List<Record>
typealias Record = Map<String, Any?>

/**
 * The interface which abstracts the details of the data access concerning posts.
 */
interface PostRepository {


    /**
     * Finds posts which matches the given conditions.
     */
    fun findPosts(input: PostSearchInput = PostSearchInput()): CompletableFuture<Set<PostEntity>>

    /**
     * Counts the number of posts which matches the given conditions.
     */
    fun countPosts(input: PostSearchInput = PostSearchInput()): CompletableFuture<Long>

    /**
     * Loads posts having given primary keys.
     */
    fun loadPosts(keys: Set<PostEntity>): CompletableFuture<Set<PostEntity>>

    /**
     * Loads posted_by
     */
    fun loadPostedByOfPost(inputs: Map<UserSearchInput, Set<PostEntity>>): CompletableFuture<Map<Pair<UserSearchInput, PostEntity>, UserEntity?>>
    /**
     * Loads comments
     */
    fun loadCommentsOfPost(inputs: Map<CommentSearchInput, Set<PostEntity>>): CompletableFuture<Map<Pair<CommentSearchInput, PostEntity>, List<CommentEntity>>>

}