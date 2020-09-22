package laplacian.tutorial.entity.post
import org.springframework.stereotype.Component
import org.springframework.data.r2dbc.core.DatabaseClient
import java.util.concurrent.CompletableFuture
import laplacian.tutorial.entity.RepositoryBase
import laplacian.tutorial.entity.user.UserEntity
import laplacian.tutorial.entity.user.UserSearchInput
import laplacian.tutorial.entity.comment.CommentEntity
import laplacian.tutorial.entity.comment.CommentSearchInput


/**
 * An implementation of the PostRepository.
 */
@Component
class PostRepositoryImpl(
    private val db: DatabaseClient
): RepositoryBase(db), PostRepository {


    /**
     * Finds posts.
     */
    override fun findPosts(input: PostSearchInput): CompletableFuture<Set<PostEntity>> =
        db.execute(
        """
        SELECT
          id
        ${queryOfFindPosts(input)}
        """.trim())
        .let { bindParamsOfFindPostsQuery(it, input) }
        .fetch()
        .all()
        .collectList()
        .map { PostEntity.fromRecords(it).toSet() }
        .toFuture()

    override fun countPosts(input: PostSearchInput): CompletableFuture<Long> =
        db
        .execute("SELECT count(*) AS count ${queryOfFindPosts(input)}")
        .let { bindParamsOfFindPostsQuery(it, input) }
        .fetch()
        .first()
        .map{ it["count"] as Long }
        .toFuture()

    fun bindParamsOfFindPostsQuery(
        sql: DatabaseClient.GenericExecuteSpec, input: PostSearchInput
    ): DatabaseClient.GenericExecuteSpec {
        var result = sql
        result = embedSearchParamsForIntField(result, input.id, "id")
        result = embedSearchParamsForIntField(result, input.userId, "userId")
        result = embedSearchParamsForStringField(result, input.title, "title")
        result = embedSearchParamsForStringField(result, input.body, "body")
        return result
    }

    fun queryOfFindPosts(input: PostSearchInput): String = """
        FROM
          t_post
        ${if (input.isEmpty()) "" else """
        WHERE
          ${searchConditionForIntField(input.id, "t_post.id", "id")}
          ${searchConditionForIntField(input.userId, "t_post.user_id", "userId")}
          ${searchConditionForStringField(input.title, "t_post.title", "title")}
          ${searchConditionForStringField(input.body, "t_post.body", "body")}
        """}
        """
        .trimMargin()
        .replace("""(\n|^)\s*(--.*)?(\n|$)""", "")
        .replace("""AND\s*$""".toRegex(), "")

    /**
     * Loads posts having the given keys.
     */
    override fun loadPosts(keys: Set<PostEntity>): CompletableFuture<Set<PostEntity>> =
        db.execute(
        """
        SELECT
          id,
          user_id,
          title,
          body
        FROM
          t_post
        WHERE
        -- ${keys.mapIndexed{ index, _ -> """
          t_post.id = :id${index}
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
        .map { PostEntity.fromRecords(it).toSet() }
        .toFuture()

    /**
     * Loads posted_by of this post.
     */
    override fun loadPostedByOfPost(
        inputs: Map<UserSearchInput, Set<PostEntity>>
    ): CompletableFuture<Map<Pair<UserSearchInput, PostEntity>, UserEntity?>> =
        db.execute(
        """
        SELECT
          ${if (inputs.isEmpty()) "" else """
          _condition_.id AS "_condition_id_",
          """}
          t_post.id AS "id",
          t_user.id AS "posted_by.id"
        FROM
          ${if (inputs.isEmpty()) "" else """
          (VALUES ${inputs.entries.mapIndexed{ i, _ -> "(${i})"}.joinToString(", ")}) _condition_(id),
          """}
          t_post,
          t_user
        WHERE
          t_post.user_id = t_user.id
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
            PostEntity.fromRecordsGrouping(records) { record ->
                val searchConditionIndex = record["_condition_id_"]
                if (searchConditionIndex == null || searchConditionIndex !is Int) {
                    UserSearchInput()
                }
                else {
                    inputs.keys.toList().getOrElse(searchConditionIndex){ UserSearchInput() }
                }
            }.flatMap { (searchCondition, posts) ->
                posts.map { post -> searchCondition to post }
            }
            .map { it to it.second.postedBy }
            .toMap()
        }
        .toFuture()
    /**
     * Loads comments of this post.
     */
    override fun loadCommentsOfPost(
        inputs: Map<CommentSearchInput, Set<PostEntity>>
    ): CompletableFuture<Map<Pair<CommentSearchInput, PostEntity>, List<CommentEntity>>> =
        db.execute(
        """
        SELECT
          ${if (inputs.isEmpty()) "" else """
          _condition_.id AS "_condition_id_",
          """}
          t_post.id AS "id",
          t_comment.post_id AS "comments.post_id",
          t_comment.seq_number AS "comments.seq_number"
        FROM
          ${if (inputs.isEmpty()) "" else """
          (VALUES ${inputs.entries.mapIndexed{ i, _ -> "(${i})"}.joinToString(", ")}) _condition_(id),
          """}
          t_post,
          t_comment
        WHERE
          t_post.id = t_comment.post_id
        AND (
        ${inputs.entries.mapIndexed{ i, (input, keys) -> """
          (${keys.mapIndexed{ index, _ -> """
            t_comment.post_id = :id_${i}_${index}
          -- """}.joinToString("\nOR\n")}
          )
          AND
            ${if (input.isEmpty()) "" else """
            ${searchConditionForIntField(input.postId, "t_comment.post_id", "postId_${i}_")}
            ${searchConditionForIntField(input.seqNumber, "t_comment.seq_number", "seqNumber_${i}_")}
            ${searchConditionForStringField(input.name, "t_comment.name", "name_${i}_")}
            ${searchConditionForStringField(input.email, "t_comment.email", "email_${i}_")}
            ${searchConditionForStringField(input.body, "t_comment.body", "body_${i}_")}
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
                sql = embedSearchParamsForIntField(sql, input.postId, "postId_${i}_")
                sql = embedSearchParamsForIntField(sql, input.seqNumber, "seqNumber_${i}_")
                sql = embedSearchParamsForStringField(sql, input.name, "name_${i}_")
                sql = embedSearchParamsForStringField(sql, input.email, "email_${i}_")
                sql = embedSearchParamsForStringField(sql, input.body, "body_${i}_")
            }
            sql
        }
        .fetch()
        .all()
        .collectList()
        .map { records ->
            PostEntity.fromRecordsGrouping(records) { record ->
                val searchConditionIndex = record["_condition_id_"]
                if (searchConditionIndex == null || searchConditionIndex !is Int) {
                    CommentSearchInput()
                }
                else {
                    inputs.keys.toList().getOrElse(searchConditionIndex){ CommentSearchInput() }
                }
            }.flatMap { (searchCondition, posts) ->
                posts.map { post -> searchCondition to post }
            }
            .map { it to it.second.comments }
            .toMap()
        }
        .toFuture()

}