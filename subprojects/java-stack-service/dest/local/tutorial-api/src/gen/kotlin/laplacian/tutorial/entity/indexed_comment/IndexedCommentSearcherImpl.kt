package laplacian.tutorial.entity.indexed_comment

import org.springframework.stereotype.Component
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates
import org.elasticsearch.index.query.QueryBuilders

import java.util.concurrent.CompletableFuture
import java.util.*

@Component
class IndexedCommentSearcherImpl(
    private val searcher: ReactiveElasticsearchOperations
): IndexedCommentSearcher {

    companion object {
        const val INDEX_NAME = "indexed_comment"
    }

    override fun findComments(input: IndexedCommentSearchInput): CompletableFuture<List<IndexedCommentDocument>> {
        val query = NativeSearchQueryBuilder()
        if (!input.body.isEmpty()) {
            query.withQuery(QueryBuilders.matchQuery("body", input.body))
        }
        if (!input.email.isEmpty()) {
            query.withQuery(QueryBuilders.matchQuery("email", input.email))
        }
        if (!input.name.isEmpty()) {
            query.withQuery(QueryBuilders.matchQuery("name", input.name))
        }
        if (!input.postId.equalsTo.isEmpty()) {
            query.withQuery(QueryBuilders.termsQuery("postId", input.postId.equalsTo))
        }
        if (input.postId.inRangeFrom != null) {
            query.withQuery(QueryBuilders.rangeQuery("postId").from(input.postId.inRangeFrom))
        }
        if (input.postId.inRangeTo != null) {
            query.withQuery(QueryBuilders.rangeQuery("postId").to(input.postId.inRangeTo))
        }
        if (!input.seqNumber.equalsTo.isEmpty()) {
            query.withQuery(QueryBuilders.termsQuery("seqNumber", input.seqNumber.equalsTo))
        }
        if (input.seqNumber.inRangeFrom != null) {
            query.withQuery(QueryBuilders.rangeQuery("seqNumber").from(input.seqNumber.inRangeFrom))
        }
        if (input.seqNumber.inRangeTo != null) {
            query.withQuery(QueryBuilders.rangeQuery("seqNumber").to(input.seqNumber.inRangeTo))
        }
        return searcher.search(
            query.build(),
            IndexedCommentDocument::class.java,
            IndexCoordinates.of(INDEX_NAME),
        )
        .map{ it.content }
        .collectList()
        .toFuture()
    }
}