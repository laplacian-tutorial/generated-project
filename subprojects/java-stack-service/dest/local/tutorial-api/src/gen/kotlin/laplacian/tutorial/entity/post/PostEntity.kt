package laplacian.tutorial.entity.post
import laplacian.tutorial.entity.user.UserEntity

import laplacian.tutorial.entity.comment.CommentEntity


import java.util.*

/**
 * post
 */
class PostEntity {
    /**
     * The id of this post.
     */
    private var _id: Int? = null
    var id: Int
        get() = _id!!
        set(v) { _id = v }
    /**
     * The user_id of this post.
     */
    private var _userId: Int? = null
    var userId: Int
        get() = _userId!!
        set(v) { _userId = v }
    /**
     * The title of this post.
     */
    lateinit var title: String
    /**
     * The body of this post.
     */
    lateinit var body: String

    /**
     * posted_by
     */
    var postedBy: UserEntity?
        get() = _postedBy
        set(value) { _postedBy = value }
    private var _postedBy: UserEntity? = null
    /**
     * comments
     */
    lateinit var comments: List<CommentEntity>

    override fun equals(other: Any?): Boolean =
        (other === this) ||
        (other != null) &&
        (other is PostEntity) &&
        Objects.equals(this.id, other.id)

    override fun hashCode(): Int = Objects.hash(
        this.id
    )

    override fun toString(): String = "PostEntity(" +
        "id: $id)"

    companion object {
        fun fromRecord(
            record: Record,
            prefix: String = ""
        ): PostEntity = PostEntity().apply {
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
                    prefix + "id" -> id = nonNullInteger(k, v)
                    prefix + "user_id" -> userId = nonNullInteger(k, v)
                    prefix + "title" -> title = nonNullString(k, v)
                    prefix + "body" -> body = nonNullString(k, v)
                }
            }
        }

        fun <T> fromRecordsGrouping(
            records: Records,
            groupBy: (Record) -> T
        ): Map<T, List<PostEntity>> {
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
        ): List<PostEntity> {
            if (records.isEmpty()) return emptyList()
            val nestingPostedBy = records.first().keys.any{ it.startsWith("postedBy.") }
            val nestingComments = records.first().keys.any{ it.startsWith("comments.") }
            return records.fold(mutableMapOf<PostEntity, MutableList<Record>>()) { acc, record ->
                val key = PostEntity.fromRecord(record, prefix)
                acc.also {
                    it.getOrPut(key) { mutableListOf<Record>() }
                      .add(record)
                }
            }.map { (post, records) ->
                if (nestingPostedBy) {
                    post.postedBy = UserEntity.fromRecords(records, "posted_by.").first()
                }
                if (nestingComments) {
                    post.comments = CommentEntity.fromRecords(records, "comments.")
                }
                post
            }
        }
    }
}