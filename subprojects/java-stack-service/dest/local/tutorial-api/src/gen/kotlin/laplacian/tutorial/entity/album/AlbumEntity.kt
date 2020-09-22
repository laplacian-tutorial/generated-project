package laplacian.tutorial.entity.album
import laplacian.tutorial.entity.user.UserEntity

import laplacian.tutorial.entity.photo.PhotoEntity


import java.util.*

/**
 * album
 */
class AlbumEntity {
    /**
     * The id of this album.
     */
    private var _id: Int? = null
    var id: Int
        get() = _id!!
        set(v) { _id = v }
    /**
     * The user_id of this album.
     */
    private var _userId: Int? = null
    var userId: Int
        get() = _userId!!
        set(v) { _userId = v }
    /**
     * The title of this album.
     */
    var title: String = ""


    /**
     * owner
     */
    var owner: UserEntity
        get() = _owner ?: UserEntity().also {
            it.id = userId
        }
        set(value) { _owner = value }
    private var _owner: UserEntity? = null
    /**
     * photos
     */
    lateinit var photos: List<PhotoEntity>

    override fun equals(other: Any?): Boolean =
        (other === this) ||
        (other != null) &&
        (other is AlbumEntity) &&
        Objects.equals(this.id, other.id)

    override fun hashCode(): Int = Objects.hash(
        this.id
    )

    override fun toString(): String = "AlbumEntity(" +
        "id: $id)"

    companion object {
        fun fromRecord(
            record: Record,
            prefix: String = ""
        ): AlbumEntity = AlbumEntity().apply {
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
                }
            }
        }

        fun <T> fromRecordsGrouping(
            records: Records,
            groupBy: (Record) -> T
        ): Map<T, List<AlbumEntity>> {
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
        ): List<AlbumEntity> {
            if (records.isEmpty()) return emptyList()
            val nestingOwner = records.first().keys.any{ it.startsWith("owner.") }
            val nestingPhotos = records.first().keys.any{ it.startsWith("photos.") }
            return records.fold(mutableMapOf<AlbumEntity, MutableList<Record>>()) { acc, record ->
                val key = AlbumEntity.fromRecord(record, prefix)
                acc.also {
                    it.getOrPut(key) { mutableListOf<Record>() }
                      .add(record)
                }
            }.map { (album, records) ->
                if (nestingOwner) {
                    album.owner = UserEntity.fromRecords(records, "owner.").first()
                }
                if (nestingPhotos) {
                    album.photos = PhotoEntity.fromRecords(records, "photos.")
                }
                album
            }
        }
    }
}