package laplacian.tutorial.entity.album
import org.springframework.stereotype.Component
import org.springframework.data.r2dbc.core.DatabaseClient
import java.util.concurrent.CompletableFuture
import laplacian.tutorial.entity.RepositoryBase
import laplacian.tutorial.entity.user.UserEntity
import laplacian.tutorial.entity.user.UserSearchInput
import laplacian.tutorial.entity.photo.PhotoEntity
import laplacian.tutorial.entity.photo.PhotoSearchInput


/**
 * An implementation of the AlbumRepository.
 */
@Component
class AlbumRepositoryImpl(
    private val db: DatabaseClient
): RepositoryBase(db), AlbumRepository {


    /**
     * Finds albums.
     */
    override fun findAlbums(input: AlbumSearchInput): CompletableFuture<Set<AlbumEntity>> =
        db.execute(
        """
        SELECT
          id
        ${queryOfFindAlbums(input)}
        """.trim())
        .let { bindParamsOfFindAlbumsQuery(it, input) }
        .fetch()
        .all()
        .collectList()
        .map { AlbumEntity.fromRecords(it).toSet() }
        .toFuture()

    override fun countAlbums(input: AlbumSearchInput): CompletableFuture<Long> =
        db
        .execute("SELECT count(*) AS count ${queryOfFindAlbums(input)}")
        .let { bindParamsOfFindAlbumsQuery(it, input) }
        .fetch()
        .first()
        .map{ it["count"] as Long }
        .toFuture()

    fun bindParamsOfFindAlbumsQuery(
        sql: DatabaseClient.GenericExecuteSpec, input: AlbumSearchInput
    ): DatabaseClient.GenericExecuteSpec {
        var result = sql
        result = embedSearchParamsForIntField(result, input.id, "id")
        result = embedSearchParamsForIntField(result, input.userId, "userId")
        result = embedSearchParamsForStringField(result, input.title, "title")
        return result
    }

    fun queryOfFindAlbums(input: AlbumSearchInput): String = """
        FROM
          t_album
        ${if (input.isEmpty()) "" else """
        WHERE
          ${searchConditionForIntField(input.id, "t_album.id", "id")}
          ${searchConditionForIntField(input.userId, "t_album.user_id", "userId")}
          ${searchConditionForStringField(input.title, "t_album.title", "title")}
        """}
        """
        .trimMargin()
        .replace("""(\n|^)\s*(--.*)?(\n|$)""", "")
        .replace("""AND\s*$""".toRegex(), "")

    /**
     * Loads albums having the given keys.
     */
    override fun loadAlbums(keys: Set<AlbumEntity>): CompletableFuture<Set<AlbumEntity>> =
        db.execute(
        """
        SELECT
          id,
          user_id,
          title
        FROM
          t_album
        WHERE
        -- ${keys.mapIndexed{ index, _ -> """
          t_album.id = :id${index}
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
        .map { AlbumEntity.fromRecords(it).toSet() }
        .toFuture()

    /**
     * Loads owner of this album.
     */
    override fun loadOwnerOfAlbum(
        inputs: Map<UserSearchInput, Set<AlbumEntity>>
    ): CompletableFuture<Map<Pair<UserSearchInput, AlbumEntity>, UserEntity>> =
        db.execute(
        """
        SELECT
          ${if (inputs.isEmpty()) "" else """
          _condition_.id AS "_condition_id_",
          """}
          t_album.id AS "id",
          t_user.id AS "owner.id"
        FROM
          ${if (inputs.isEmpty()) "" else """
          (VALUES ${inputs.entries.mapIndexed{ i, _ -> "(${i})"}.joinToString(", ")}) _condition_(id),
          """}
          t_album,
          t_user
        WHERE
          t_album.user_id = t_user.id
        AND (
        ${inputs.entries.mapIndexed{ i, (input, keys) -> """
          (${keys.mapIndexed{ index, _ -> """
            t_user.id = :userId_${i}_${index}
          -- """}.joinToString("\nOR\n")}
          )
          AND
            ${if (input.isEmpty()) "" else """
            ${searchConditionForIntField(input.id, "t_user.id", "id_${i}_")}
            ${searchConditionForStringField(input.name, "t_user.name", "name_${i}_")}
            ${searchConditionForStringField(input.username, "t_user.username", "username_${i}_")}
            ${searchConditionForStringField(input.email, "t_user.email", "email_${i}_")}
            ${searchConditionForStringField(input.phone, "t_user.phone", "phone_${i}_")}
            ${searchConditionForStringField(input.website, "t_user.website", "website_${i}_")}
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
                    binder = binder.bind("userId_${i}_${index}", key.userId)
                    binder
                }
                sql = embedSearchParamsForIntField(sql, input.id, "id_${i}_")
                sql = embedSearchParamsForStringField(sql, input.name, "name_${i}_")
                sql = embedSearchParamsForStringField(sql, input.username, "username_${i}_")
                sql = embedSearchParamsForStringField(sql, input.email, "email_${i}_")
                sql = embedSearchParamsForStringField(sql, input.phone, "phone_${i}_")
                sql = embedSearchParamsForStringField(sql, input.website, "website_${i}_")
            }
            sql
        }
        .fetch()
        .all()
        .collectList()
        .map { records ->
            AlbumEntity.fromRecordsGrouping(records) { record ->
                val searchConditionIndex = record["_condition_id_"]
                if (searchConditionIndex == null || searchConditionIndex !is Int) {
                    UserSearchInput()
                }
                else {
                    inputs.keys.toList().getOrElse(searchConditionIndex){ UserSearchInput() }
                }
            }.flatMap { (searchCondition, albums) ->
                albums.map { album -> searchCondition to album }
            }
            .map { it to it.second.owner }
            .toMap()
        }
        .toFuture()
    /**
     * Loads photos of this album.
     */
    override fun loadPhotosOfAlbum(
        inputs: Map<PhotoSearchInput, Set<AlbumEntity>>
    ): CompletableFuture<Map<Pair<PhotoSearchInput, AlbumEntity>, List<PhotoEntity>>> =
        db.execute(
        """
        SELECT
          ${if (inputs.isEmpty()) "" else """
          _condition_.id AS "_condition_id_",
          """}
          t_album.id AS "id",
          t_photo.id AS "photos.id"
        FROM
          ${if (inputs.isEmpty()) "" else """
          (VALUES ${inputs.entries.mapIndexed{ i, _ -> "(${i})"}.joinToString(", ")}) _condition_(id),
          """}
          t_album,
          t_photo
        WHERE
          t_album.id = t_photo.album_id
        AND (
        ${inputs.entries.mapIndexed{ i, (input, keys) -> """
          (${keys.mapIndexed{ index, _ -> """
            t_photo.album_id = :id_${i}_${index}
          -- """}.joinToString("\nOR\n")}
          )
          AND
            ${if (input.isEmpty()) "" else """
            ${searchConditionForIntField(input.id, "t_photo.id", "id_${i}_")}
            ${searchConditionForIntField(input.albumId, "t_photo.album_id", "albumId_${i}_")}
            ${searchConditionForStringField(input.title, "t_photo.title", "title_${i}_")}
            ${searchConditionForStringField(input.url, "t_photo.url", "url_${i}_")}
            ${searchConditionForStringField(input.thumbnailUrl, "t_photo.thumbnail_url", "thumbnailUrl_${i}_")}
            ${searchConditionForStringField(input.dateTaken, "t_photo.date_taken", "dateTaken_${i}_")}
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
                    binder = binder.bind("id_${i}_${index}", key.id)
                    binder
                }
                sql = embedSearchParamsForIntField(sql, input.id, "id_${i}_")
                sql = embedSearchParamsForIntField(sql, input.albumId, "albumId_${i}_")
                sql = embedSearchParamsForStringField(sql, input.title, "title_${i}_")
                sql = embedSearchParamsForStringField(sql, input.url, "url_${i}_")
                sql = embedSearchParamsForStringField(sql, input.thumbnailUrl, "thumbnailUrl_${i}_")
                sql = embedSearchParamsForStringField(sql, input.dateTaken, "dateTaken_${i}_")
            }
            sql
        }
        .fetch()
        .all()
        .collectList()
        .map { records ->
            AlbumEntity.fromRecordsGrouping(records) { record ->
                val searchConditionIndex = record["_condition_id_"]
                if (searchConditionIndex == null || searchConditionIndex !is Int) {
                    PhotoSearchInput()
                }
                else {
                    inputs.keys.toList().getOrElse(searchConditionIndex){ PhotoSearchInput() }
                }
            }.flatMap { (searchCondition, albums) ->
                albums.map { album -> searchCondition to album }
            }
            .map { it to it.second.photos }
            .toMap()
        }
        .toFuture()

}