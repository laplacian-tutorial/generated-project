package laplacian.tutorial.entity.photo
import org.springframework.stereotype.Component
import org.springframework.data.r2dbc.core.DatabaseClient
import java.util.concurrent.CompletableFuture
import laplacian.tutorial.entity.RepositoryBase
import laplacian.tutorial.entity.album.AlbumEntity
import laplacian.tutorial.entity.album.AlbumSearchInput


/**
 * An implementation of the PhotoRepository.
 */
@Component
class PhotoRepositoryImpl(
    private val db: DatabaseClient
): RepositoryBase(db), PhotoRepository {


    /**
     * Finds photos.
     */
    override fun findPhotos(input: PhotoSearchInput): CompletableFuture<Set<PhotoEntity>> =
        db.execute(
        """
        SELECT
          id
        ${queryOfFindPhotos(input)}
        """.trim())
        .let { bindParamsOfFindPhotosQuery(it, input) }
        .fetch()
        .all()
        .collectList()
        .map { PhotoEntity.fromRecords(it).toSet() }
        .toFuture()

    override fun countPhotos(input: PhotoSearchInput): CompletableFuture<Long> =
        db
        .execute("SELECT count(*) AS count ${queryOfFindPhotos(input)}")
        .let { bindParamsOfFindPhotosQuery(it, input) }
        .fetch()
        .first()
        .map{ it["count"] as Long }
        .toFuture()

    fun bindParamsOfFindPhotosQuery(
        sql: DatabaseClient.GenericExecuteSpec, input: PhotoSearchInput
    ): DatabaseClient.GenericExecuteSpec {
        var result = sql
        result = embedSearchParamsForIntField(result, input.id, "id")
        result = embedSearchParamsForIntField(result, input.albumId, "albumId")
        result = embedSearchParamsForStringField(result, input.title, "title")
        result = embedSearchParamsForStringField(result, input.url, "url")
        result = embedSearchParamsForStringField(result, input.thumbnailUrl, "thumbnailUrl")
        result = embedSearchParamsForStringField(result, input.dateTaken, "dateTaken")
        return result
    }

    fun queryOfFindPhotos(input: PhotoSearchInput): String = """
        FROM
          t_photo
        ${if (input.isEmpty()) "" else """
        WHERE
          ${searchConditionForIntField(input.id, "t_photo.id", "id")}
          ${searchConditionForIntField(input.albumId, "t_photo.album_id", "albumId")}
          ${searchConditionForStringField(input.title, "t_photo.title", "title")}
          ${searchConditionForStringField(input.url, "t_photo.url", "url")}
          ${searchConditionForStringField(input.thumbnailUrl, "t_photo.thumbnail_url", "thumbnailUrl")}
          ${searchConditionForStringField(input.dateTaken, "t_photo.date_taken", "dateTaken")}
        """}
        """
        .trimMargin()
        .replace("""(\n|^)\s*(--.*)?(\n|$)""", "")
        .replace("""AND\s*$""".toRegex(), "")

    /**
     * Loads photos having the given keys.
     */
    override fun loadPhotos(keys: Set<PhotoEntity>): CompletableFuture<Set<PhotoEntity>> =
        db.execute(
        """
        SELECT
          id,
          album_id,
          title,
          url,
          thumbnail_url,
          date_taken
        FROM
          t_photo
        WHERE
        -- ${keys.mapIndexed{ index, _ -> """
          t_photo.id = :id${index}
        -- """}.joinToString("\nOR\n")}
        """.trimMargin())
        .let { sql ->
            keys.foldIndexed(sql){ index, acc, key ->
                var binder = acc
                binder = binder.bind("id${index}", key.id)
                binder
            }
        }
        .fetch()
        .all()
        .collectList()
        .map { PhotoEntity.fromRecords(it).toSet() }
        .toFuture()

    /**
     * Loads album of this photo.
     */
    override fun loadAlbumOfPhoto(
        inputs: Map<AlbumSearchInput, Set<PhotoEntity>>
    ): CompletableFuture<Map<Pair<AlbumSearchInput, PhotoEntity>, AlbumEntity>> =
        db.execute(
        """
        SELECT
          ${if (inputs.isEmpty()) "" else """
          _condition_.id AS "_condition_id_",
          """}
          t_photo.id AS "id",
          t_album.id AS "album.id"
        FROM
          ${if (inputs.isEmpty()) "" else """
          (VALUES ${inputs.entries.mapIndexed{ i, _ -> "(${i})"}.joinToString(", ")}) _condition_(id),
          """}
          t_photo,
          t_album
        WHERE
          t_photo.album_id = t_album.id
        AND (
        ${inputs.entries.mapIndexed{ i, (input, keys) -> """
          (${keys.mapIndexed{ index, _ -> """
            t_album.id = :albumId_${i}_${index}
          -- """}.joinToString("\nOR\n")}
          )
          AND
            ${if (input.isEmpty()) "" else """
            ${searchConditionForIntField(input.id, "t_album.id", "id_${i}_")}
            ${searchConditionForIntField(input.userId, "t_album.user_id", "userId_${i}_")}
            ${searchConditionForStringField(input.title, "t_album.title", "title_${i}_")}
            """}
             _condition_.id = ${i}
        """}.joinToString("\nOR\n")}
         )
        """
        .trimMargin()
        .replace("""(\n|^)\s*(--.*)?(\n|$)""", "")
        .replace("""AND\s*$""".toRegex(), ""))
        .let {
            var sql = it
            inputs.entries.mapIndexed{ i, (input, keys) ->
                sql = keys.foldIndexed(sql) { index, acc, key ->
                    var binder = acc
                    binder = binder.bind("albumId_${i}_${index}", key.albumId)
                    binder
                }
                sql = embedSearchParamsForIntField(sql, input.id, "id_${i}_")
                sql = embedSearchParamsForIntField(sql, input.userId, "userId_${i}_")
                sql = embedSearchParamsForStringField(sql, input.title, "title_${i}_")
            }
            sql
        }
        .fetch()
        .all()
        .collectList()
        .map { records ->
            PhotoEntity.fromRecordsGrouping(records) { record ->
                val searchConditionIndex = record["_condition_id_"]
                if (searchConditionIndex == null || searchConditionIndex !is Int) {
                    AlbumSearchInput()
                }
                else {
                    inputs.keys.toList().getOrElse(searchConditionIndex){ AlbumSearchInput() }
                }
            }.flatMap { (searchCondition, photos) ->
                photos.map { photo -> searchCondition to photo }
            }
            .map { it to it.second.album }
            .toMap()
        }
        .toFuture()

}