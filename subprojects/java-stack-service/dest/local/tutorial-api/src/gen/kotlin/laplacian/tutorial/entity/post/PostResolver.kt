package laplacian.tutorial.entity.post
import laplacian.tutorial.entity.user.UserEntity
import laplacian.tutorial.entity.user.UserSearchInput
import laplacian.tutorial.entity.comment.CommentEntity
import laplacian.tutorial.entity.comment.CommentSearchInput

import org.dataloader.DataLoader
import org.dataloader.DataLoaderRegistry
import org.springframework.stereotype.Component
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.TypeRuntimeWiring
import graphql.schema.DataFetchingEnvironment
import java.util.concurrent.CompletableFuture

/**
 * An reactive implementation of the post entity query resolver.
 */
@Component
class PostResolver(
    private val postRepository: PostRepository
) {

    /**
     * Loads the user_id of this post.
     */
    fun userId(self: PostEntity, context: DataFetchingEnvironment): CompletableFuture<Int> =
        context
        .getDataLoader<PostEntity, PostEntity>(BY_PK)
        .load(self)
        .thenApply<Int>{ it.userId }
    /**
     * Loads the title of this post.
     */
    fun title(self: PostEntity, context: DataFetchingEnvironment): CompletableFuture<String> =
        context
        .getDataLoader<PostEntity, PostEntity>(BY_PK)
        .load(self)
        .thenApply<String>{ it.title }
    /**
     * Loads the body of this post.
     */
    fun body(self: PostEntity, context: DataFetchingEnvironment): CompletableFuture<String> =
        context
        .getDataLoader<PostEntity, PostEntity>(BY_PK)
        .load(self)
        .thenApply<String>{ it.body }

    /**
     * Loads the posted_by of this post.
     */
    fun postedBy(self: PostEntity, context: DataFetchingEnvironment): CompletableFuture<UserEntity> =
        context
        .getDataLoader<Pair<UserSearchInput, PostEntity>, UserEntity>(POSTED_BY)
        .load(UserSearchInput.from(context.arguments) to self)
    /**
     * Loads the comments of this post.
     */
    fun comments(self: PostEntity, context: DataFetchingEnvironment): CompletableFuture<List<CommentEntity>> =
        context
        .getDataLoader<Pair<CommentSearchInput, PostEntity>, List<CommentEntity>>(COMMENTS)
        .load(CommentSearchInput.from(context.arguments) to self)
        .thenApply<List<CommentEntity>>{ it ?: emptyList() }

    fun registerLoader(registry: DataLoaderRegistry) {
        registry.register(BY_PK, DataLoader.newMappedDataLoader<PostEntity, PostEntity> { keys ->
            postRepository
            .loadPosts(keys)
            .thenApply{ set -> set.map{ (it to it) }.toMap() }
        })
        registry.register(POSTED_BY, DataLoader.newMappedDataLoader<Pair<UserSearchInput, PostEntity>, UserEntity> { entries ->
            val input = entries.fold(mutableMapOf<UserSearchInput, MutableSet<PostEntity>>()) { acc, (userSearchInput, post) ->
                val posts = acc.getOrPut(userSearchInput) { mutableSetOf<PostEntity>() }
                posts.add(post)
                acc
            }
            postRepository
            .loadPostedByOfPost(input)
        })
        registry.register(COMMENTS, DataLoader.newMappedDataLoader<Pair<CommentSearchInput, PostEntity>, List<CommentEntity>> { entries ->
            val input = entries.fold(mutableMapOf<CommentSearchInput, MutableSet<PostEntity>>()) { acc, (commentSearchInput, post) ->
                val posts = acc.getOrPut(commentSearchInput) { mutableSetOf<PostEntity>() }
                posts.add(post)
                acc
            }
            postRepository
            .loadCommentsOfPost(input)
        })
    }

    fun registerFetcher(wiring: RuntimeWiring.Builder) = wiring.type(
        TypeRuntimeWiring.newTypeWiring("Post")
        .dataFetcher("userId") { env ->
            val key = env.getSource() as PostEntity
            userId(key, env)
        }
        .dataFetcher("title") { env ->
            val key = env.getSource() as PostEntity
            title(key, env)
        }
        .dataFetcher("body") { env ->
            val key = env.getSource() as PostEntity
            body(key, env)
        }
        .dataFetcher("postedBy") { env ->
            val key = env.getSource() as PostEntity
            postedBy(key, env)
        }
        .dataFetcher("comments") { env ->
            val key = env.getSource() as PostEntity
            comments(key, env)
        }
    )

    companion object {
        const val BY_PK = "post_byPK"
        const val POSTED_BY = "post_postedBy"
        const val COMMENTS = "post_comments"
    }
}