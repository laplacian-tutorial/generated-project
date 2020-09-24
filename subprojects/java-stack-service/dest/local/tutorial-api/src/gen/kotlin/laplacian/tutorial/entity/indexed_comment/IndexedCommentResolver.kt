package laplacian.tutorial.entity.indexed_comment

import org.dataloader.DataLoader
import org.dataloader.DataLoaderRegistry
import org.springframework.stereotype.Component
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.TypeRuntimeWiring
import graphql.schema.DataFetchingEnvironment
import java.util.concurrent.CompletableFuture
import laplacian.tutorial.entity.comment.*


/**
 * A reactive implementation of the indexed_comment entity query resolver.
 */
@Component
class IndexedCommentResolver(
    private val commentRepository: CommentRepository,
) {
    fun detail(
        self: IndexedCommentDocument,
        context: DataFetchingEnvironment
    ): CompletableFuture<CommentEntity> =
        context
        .getDataLoader<CommentEntity, CommentEntity>(BY_PK)
        .load(CommentEntity().apply {
            postId = self.postId
            seqNumber = self.seqNumber
        })

    fun registerFetcher(wiring: RuntimeWiring.Builder) = wiring.type(
        TypeRuntimeWiring.newTypeWiring("IndexedComment")
        .dataFetcher("detail") { env ->
            val key = env.getSource() as IndexedCommentDocument
            detail(key, env)
        }
    )

    companion object {
        const val BY_PK = "comment_byPK"
    }
}