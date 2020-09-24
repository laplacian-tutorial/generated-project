package laplacian.tutorial.entity.indexed_comment

import org.springframework.stereotype.Component
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations

import java.util.concurrent.CompletableFuture
import java.util.*

interface IndexedCommentSearcher {
    /**
     * Finds indexed_comments which matches the given conditions.
     */
    fun findIndexedComments(input: IndexedCommentSearchInput = IndexedCommentSearchInput()): CompletableFuture<List<IndexedCommentDocument>>
}