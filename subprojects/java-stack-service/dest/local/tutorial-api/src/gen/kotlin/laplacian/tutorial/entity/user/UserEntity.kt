package laplacian.tutorial.entity.user
import laplacian.tutorial.entity.task.TaskEntity

import laplacian.tutorial.entity.album.AlbumEntity

import laplacian.tutorial.entity.post.PostEntity


import java.util.*

/**
 * user
 */
class UserEntity {
    /**
     * The id of this user.
     */
    private var _id: Int? = null
    var id: Int
        get() = _id!!
        set(v) { _id = v }
    /**
     * The name of this user.
     */
    lateinit var name: String
    /**
     * The username of this user.
     */
    lateinit var username: String
    /**
     * The email of this user.
     */
    lateinit var email: String
    /**
     * The phone of this user.
     */
    var phone: String? = null
    /**
     * The website of this user.
     */
    var website: String? = null

    /**
     * address
     */
    var address: AddressEntity
        get() = _address ?: AddressEntity().also {
        }
        set(value) { _address = value }
    private var _address: AddressEntity? = null
    /**
     * company
     */
    var company: CompanyEntity?
        get() = _company
        set(value) { _company = value }
    private var _company: CompanyEntity? = null
    /**
     * tasks
     */
    lateinit var tasks: List<TaskEntity>
    /**
     * albums
     */
    lateinit var albums: List<AlbumEntity>
    /**
     * posts
     */
    lateinit var posts: List<PostEntity>

    override fun equals(other: Any?): Boolean =
        (other === this) ||
        (other != null) &&
        (other is UserEntity) &&
        Objects.equals(this.id, other.id)

    override fun hashCode(): Int = Objects.hash(
        this.id
    )

    override fun toString(): String = "UserEntity(" +
        "id: $id)"

    companion object {
        fun fromRecord(
            record: Record,
            prefix: String = ""
        ): UserEntity = UserEntity().apply {
            val nonNullString = {key: String, value: Any? ->
                value?.toString() ?: throw IllegalArgumentException(
                    "$key must not be null: $record"
                )
            }
            val nullableString = {_: String, value: Any? -> value?.toString()}
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
                    prefix + "name" -> name = nonNullString(k, v)
                    prefix + "username" -> username = nonNullString(k, v)
                    prefix + "email" -> email = nonNullString(k, v)
                    prefix + "phone" -> phone = nullableString(k, v)
                    prefix + "website" -> website = nullableString(k, v)
                }
            }
        }

        fun <T> fromRecordsGrouping(
            records: Records,
            groupBy: (Record) -> T
        ): Map<T, List<UserEntity>> {
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
        ): List<UserEntity> {
            if (records.isEmpty()) return emptyList()
            val nestingAddress = records.first().keys.any{ it.startsWith("address.") }
            val nestingCompany = records.first().keys.any{ it.startsWith("company.") }
            val nestingTasks = records.first().keys.any{ it.startsWith("tasks.") }
            val nestingAlbums = records.first().keys.any{ it.startsWith("albums.") }
            val nestingPosts = records.first().keys.any{ it.startsWith("posts.") }
            return records.fold(mutableMapOf<UserEntity, MutableList<Record>>()) { acc, record ->
                val key = UserEntity.fromRecord(record, prefix)
                acc.also {
                    it.getOrPut(key) { mutableListOf<Record>() }
                      .add(record)
                }
            }.map { (user, records) ->
                if (nestingAddress) {
                    user.address = AddressEntity.fromRecords(records, "address.").first()
                }
                if (nestingCompany) {
                    user.company = CompanyEntity.fromRecords(records, "company.").first()
                }
                if (nestingTasks) {
                    user.tasks = TaskEntity.fromRecords(records, "tasks.")
                }
                if (nestingAlbums) {
                    user.albums = AlbumEntity.fromRecords(records, "albums.")
                }
                if (nestingPosts) {
                    user.posts = PostEntity.fromRecords(records, "posts.")
                }
                user
            }
        }
    }
}