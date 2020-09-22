package laplacian.tutorial.entity.photo
import laplacian.tutorial.entity.album.AlbumEntity


import java.util.*

/**
 * photo
 */
class PhotoEntity {
    /**
     * The id of this photo.
     */
    private var _id: Int? = null
    var id: Int
        get() = _id!!
        set(v) { _id = v }
    /**
     * The album_id of this photo.
     */
    private var _albumId: Int? = null
    var albumId: Int
        get() = _albumId!!
        set(v) { _albumId = v }
    /**
     * The title of this photo.
     */
    var title: String = ""

    /**
     * The url of this photo.
     */
    lateinit var url: String
    /**
     * The thumbnailUrl of this photo.
     */
    lateinit var thumbnailUrl: String
    /**
     * The date_taken of this photo.
     */
    lateinit var dateTaken: String

    /**
     * album
     */
    var album: AlbumEntity
        get() = _album ?: AlbumEntity().also {
            it.id = albumId
        }
        set(value) { _album = value }
    private var _album: AlbumEntity? = null

    override fun equals(other: Any?): Boolean =
        (other === this) ||
        (other != null) &&
        (other is PhotoEntity) &&
        Objects.equals(this.id, other.id)

    override fun hashCode(): Int = Objects.hash(
        this.id
    )

    override fun toString(): String = "PhotoEntity(" +
        "id: $id)"

    companion object {
        fun fromRecord(
            record: Record,
            prefix: String = ""
        ): PhotoEntity = PhotoEntity().apply {
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
                    prefix + "album_id" -> albumId = nonNullInteger(k, v)
                    prefix + "title" -> title = nonNullString(k, v)
                    prefix + "url" -> url = nonNullString(k, v)
                    prefix + "thumbnail_url" -> thumbnailUrl = nonNullString(k, v)
                    prefix + "date_taken" -> dateTaken = nonNullString(k, v)
                }
            }
        }

        fun <T> fromRecordsGrouping(
            records: Records,
            groupBy: (Record) -> T
        ): Map<T, List<PhotoEntity>> {
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
        ): List<PhotoEntity> {
            if (records.isEmpty()) return emptyList()
            val nestingAlbum = records.first().keys.any{ it.startsWith("album.") }
            return records.fold(mutableMapOf<PhotoEntity, MutableList<Record>>()) { acc, record ->
                val key = PhotoEntity.fromRecord(record, prefix)
                acc.also {
                    it.getOrPut(key) { mutableListOf<Record>() }
                      .add(record)
                }
            }.map { (photo, records) ->
                if (nestingAlbum) {
                    photo.album = AlbumEntity.fromRecords(records, "album.").first()
                }
                photo
            }
        }
    }
}