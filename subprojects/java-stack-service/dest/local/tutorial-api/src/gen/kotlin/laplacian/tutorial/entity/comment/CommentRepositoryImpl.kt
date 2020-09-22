package laplacian.tutorial.entity.comment
import org.springframework.stereotype.Component
import org.springframework.data.r2dbc.core.DatabaseClient
import java.util.concurrent.CompletableFuture
import laplacian.tutorial.entity.RepositoryBase
import laplacian.tutorial.entity.post.PostEntity
import laplacian.tutorial.entity.post.PostSearchInput


/**
 * An implementation of the CommentRepository.
 */
@Component
class CommentRepositoryImpl(
    private val db: DatabaseClient
): RepositoryBase(db), CommentRepository {


    /**
     * Finds comments.
     */
    override fun findComments(input: CommentSearchInput): CompletableFuture<Set<CommentEntity>> =
        db.execute(
        """
        SELECT
          post_id,
          seq_number
        ${queryOfFindComments(input)}
        """.trim())
        .let { bindParamsOfFindCommentsQuery(it, input) }
        .fetch()
        .all()
        .collectList()
        .map { CommentEntity.fromRecords(it).toSet() }
        .toFuture()

    override fun countComments(input: CommentSearchInput): CompletableFuture<Long> =
        db
        .execute("SELECT count(*) AS count ${queryOfFindComments(input)}")
        .let { bindParamsOfFindCommentsQuery(it, input) }
        .fetch()
        .first()
        .map{ it["count"] as Long }
        .toFuture()

    fun bindParamsOfFindCommentsQuery(
        sql: DatabaseClient.GenericExecuteSpec, input: CommentSearchInput
    ): DatabaseClient.GenericExecuteSpec {
        var result = sql
        result = embedSearchParamsForIntField(result, input.postId, "postId")
        result = embedSearchParamsForIntField(result, input.seqNumber, "seqNumber")
        result = embedSearchParamsForStringField(result, input.name, "name")
        result = embedSearchParamsForStringField(result, input.email, "email")
        result = embedSearchParamsForStringField(result, input.body, "body")
        return result
    }

    fun queryOfFindComments(input: CommentSearchInput): String = """
        FROM
          t_comment
        ${if (input.isEmpty()) "" else """
        WHERE
          ${searchConditionForIntField(input.postId, "t_comment.post_id", "postId")}
          ${searchConditionForIntField(input.seqNumber, "t_comment.seq_number", "seqNumber")}
          ${searchConditionForStringField(input.name, "t_comment.name", "name")}
          ${searchConditionForStringField(input.email, "t_comment.email", "email")}
          ${searchConditionForStringField(input.body, "t_comment.body", "body")}
        """}
        """
        .trimMargin()
        .replace("""(\n|^)\s*(--.*)?(\n|$)""", "")
        .replace("""AND\s*$""".toRegex(), "")

    /**
     * Loads comments having the given keys.
     */
    override fun loadComments(keys: Set<CommentEntity>): CompletableFuture<Set<CommentEntity>> =
        db.execute(
        """
        SELECT
          post_id,
          seq_number,
          name,
          email,
          body
        FROM
          t_comment
        WHERE
        -- ${keys.mapIndexed{ index, _ -> """
          t_comment.post_id = :postId${index}
          AND
          t_comment.seq_number = :seqNumber${index}
        -- """}.joinToString("\nOR\n")}
        """.trimMargin())
        .let { sql ->
            keys.foldIndexed(sql){ index, acc, key ->
                var binder = acc
                binder = binder.bind("postId${index}", key.postId)
                binder = binder.bind("seqNumber${index}", key.seqNumber)
                binder
            }
        }
        .fetch()
        .all()
        .collectList()
        .map { CommentEntity.fromRecords(it).toSet() }
        .toFuture()

    /**
     * Loads post of this comment.
     */
    override fun loadPostOfComment(
        inputs: Map<PostSearchInput, Set<CommentEntity>>
    ): CompletableFuture<Map<Pair<PostSearchInput, CommentEntity>, PostEntity>> =
        db.execute(
        """
        SELECT
          ${if (inputs.isEmpty()) "" else """
          _condition_.id AS "_condition_id_",
          """}
          t_comment.post_id AS "post_id",
          t_comment.seq_number AS "seq_number",
          t_post.id AS "post.id"
        FROM
          ${if (inputs.isEmpty()) "" else """
          (VALUES ${inputs.entries.mapIndexed{ i, _ -> "(${i})"}.joinToString(", ")}) _condition_(id),
          """}
          t_comment,
          t_post
        WHERE
          t_comment.post_id = t_post.id
        AND (
        ${inputs.entries.mapIndexed{ i, (input, keys) -> """
          (${keys.mapIndexed{ index, _ -> """
            t_post.id = :postId_${i}_${index}
          -- """}.joinToString("\nOR\n")}
          )
          AND
            ${if (input.isEmpty()) "" else """
            ${searchConditionForIntField(input.id, "t_post.id", "id_${i}_")}
            ${searchConditionForIntField(input.userId, "t_post.user_id", "userId_${i}_")}
            ${searchConditionForStringField(input.title, "t_post.title", "title_${i}_")}
            ${searchConditionForStringField(input.body, "t_post.body", "body_${i}_")}
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
                    binder = binder.bind("postId_${i}_${index}", key.postId)
                    binder
                }
                sql = embedSearchParamsForIntField(sql, input.id, "id_${i}_")
                sql = embedSearchParamsForIntField(sql, input.userId, "userId_${i}_")
                sql = embedSearchParamsForStringField(sql, input.title, "title_${i}_")
                sql = embedSearchParamsForStringField(sql, input.body, "body_${i}_")
            }
            sql
        }
        .fetch()
        .all()
        .collectList()
        .map { records ->
            CommentEntity.fromRecordsGrouping(records) { record ->
                val searchConditionIndex = record["_condition_id_"]
                if (searchConditionIndex == null || searchConditionIndex !is Int) {
                    PostSearchInput()
                }
                else {
                    inputs.keys.toList().getOrElse(searchConditionIndex){ PostSearchInput() }
                }
            }.flatMap { (searchCondition, comments) ->
                comments.map { comment -> searchCondition to comment }
            }
            .map { it to it.second.post }
            .toMap()
        }
        .toFuture()

}