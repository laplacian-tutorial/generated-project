package laplacian.tutorial.entity.comment
import laplacian.tutorial.entity.post.PostEntity
import laplacian.tutorial.entity.post.PostSearchInput

import org.dataloader.DataLoader
import org.dataloader.DataLoaderRegistry
import org.springframework.stereotype.Component
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.TypeRuntimeWiring
import graphql.schema.DataFetchingEnvironment
import java.util.concurrent.CompletableFuture

/**
 * An reactive implementation of the comment entity query resolver.
 */
@Component
class CommentResolver(
    private val commentRepository: CommentRepository
) {

    /**
     * Loads the name of this comment.
     */
    fun name(self: CommentEntity, context: DataFetchingEnvironment): CompletableFuture<String> =
        context
        .getDataLoader<CommentEntity, CommentEntity>(BY_PK)
        .load(self)
        .thenApply<String>{ it.name }
    /**
     * Loads the email of this comment.
     */
    fun email(self: CommentEntity, context: DataFetchingEnvironment): CompletableFuture<String> =
        context
        .getDataLoader<CommentEntity, CommentEntity>(BY_PK)
        .load(self)
        .thenApply<String>{ it.email }
    /**
     * Loads the body of this comment.
     */
    fun body(self: CommentEntity, context: DataFetchingEnvironment): CompletableFuture<String> =
        context
        .getDataLoader<CommentEntity, CommentEntity>(BY_PK)
        .load(self)
        .thenApply<String>{ it.body }

    /**
     * Loads the post of this comment.
     */
    fun post(self: CommentEntity, context: DataFetchingEnvironment): CompletableFuture<PostEntity> =
        context
        .getDataLoader<Pair<PostSearchInput, CommentEntity>, PostEntity>(POST)
        .load(PostSearchInput.from(context.arguments) to self)

    fun registerLoader(registry: DataLoaderRegistry) {
        registry.register(BY_PK, DataLoader.newMappedDataLoader<CommentEntity, CommentEntity> { keys ->
            commentRepository
            .loadComments(keys)
            .thenApply{ set -> set.map{ (it to it) }.toMap() }
        })
        registry.register(POST, DataLoader.newMappedDataLoader<Pair<PostSearchInput, CommentEntity>, PostEntity> { entries ->
            val input = entries.fold(mutableMapOf<PostSearchInput, MutableSet<CommentEntity>>()) { acc, (postSearchInput, comment) ->
                val comments = acc.getOrPut(postSearchInput) { mutableSetOf<CommentEntity>() }
                comments.add(comment)
                acc
            }
            commentRepository
            .loadPostOfComment(input)
        })
    }

    fun registerFetcher(wiring: RuntimeWiring.Builder) = wiring.type(
        TypeRuntimeWiring.newTypeWiring("Comment")
        .dataFetcher("name") { env ->
            val key = env.getSource() as CommentEntity
            name(key, env)
        }
        .dataFetcher("email") { env ->
            val key = env.getSource() as CommentEntity
            email(key, env)
        }
        .dataFetcher("body") { env ->
            val key = env.getSource() as CommentEntity
            body(key, env)
        }
        .dataFetcher("post") { env ->
            val key = env.getSource() as CommentEntity
            post(key, env)
        }
    )

    companion object {
        const val BY_PK = "comment_byPK"
        const val POST = "comment_post"
    }
}