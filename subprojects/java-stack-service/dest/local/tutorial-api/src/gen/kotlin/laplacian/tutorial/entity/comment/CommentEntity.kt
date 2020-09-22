package laplacian.tutorial.entity.comment
import laplacian.tutorial.entity.post.PostEntity


import java.util.*

/**
 * comment
 */
class CommentEntity {
    /**
     * The post_id of this comment.
     */
    private var _postId: Int? = null
    var postId: Int
        get() = _postId!!
        set(v) { _postId = v }
    /**
     * The seq_number of this comment.
     */
    private var _seqNumber: Int? = null
    var seqNumber: Int
        get() = _seqNumber!!
        set(v) { _seqNumber = v }
    /**
     * The name of this comment.
     */
    lateinit var name: String
    /**
     * The email of this comment.
     */
    lateinit var email: String
    /**
     * The body of this comment.
     */
    lateinit var body: String

    /**
     * post
     */
    var post: PostEntity
        get() = _post ?: PostEntity().also {
            it.id = postId
        }
        set(value) { _post = value }
    private var _post: PostEntity? = null

    override fun equals(other: Any?): Boolean =
        (other === this) ||
        (other != null) &&
        (other is CommentEntity) &&
        Objects.equals(this.postId, other.postId) &&
        Objects.equals(this.seqNumber, other.seqNumber)

    override fun hashCode(): Int = Objects.hash(
        this.postId,
        this.seqNumber
    )

    override fun toString(): String = "CommentEntity(" +
        "postId: $postId, " +
        "seqNumber: $seqNumber)"

    companion object {
        fun fromRecord(
            record: Record,
            prefix: String = ""
        ): CommentEntity = CommentEntity().apply {
            val nonNullString = {key: String, value: Any? ->
                value?.toString() ?: throw IllegalArgumentException(
                    "$key must not be null: $record"
                )
            }
            val nonNullInteger = {key: String, value: Any? ->
                if (value is Int)
                  value
                else
                  value?.toString()?.toInt() ?: throw IllegalArgumentException(
                    "$key must not be null: $record"
                  )
            }
            record.forEach { k, v ->
                when(k) {
                    prefix + "post_id" -> postId = nonNullInteger(k, v)
                    prefix + "seq_number" -> seqNumber = nonNullInteger(k, v)
                    prefix + "name" -> name = nonNullString(k, v)
                    prefix + "email" -> email = nonNullString(k, v)
                    prefix + "body" -> body = nonNullString(k, v)
                }
            }
        }

        fun <T> fromRecordsGrouping(
            records: Records,
            groupBy: (Record) -> T
        ): Map<T, List<CommentEntity>> {
            if (records.isEmpty()) return emptyMap()
            return records.fold(mutableMapOf<T, MutableList<Record>>()) { acc, record ->
                val key = groupBy(record)
                acc.getOrPut(key) { mutableListOf<Record>() }
                   .add(record)
                acc
            }.mapValues { (_, records) ->
                fromRecords(records)
            }
        }

        fun fromRecords(
            records: Records,
            prefix: String = ""
        ): List<CommentEntity> {
            if (records.isEmpty()) return emptyList()
            val nestingPost = records.first().keys.any{ it.startsWith("post.") }
            return records.fold(mutableMapOf<CommentEntity, MutableList<Record>>()) { acc, record ->
                val key = CommentEntity.fromRecord(record, prefix)
                acc.also {
                    it.getOrPut(key) { mutableListOf<Record>() }
                      .add(record)
                }
            }.map { (comment, records) ->
                if (nestingPost) {
                    comment.post = PostEntity.fromRecords(records, "post.").first()
                }
                comment
            }
        }
    }
}