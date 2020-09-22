package laplacian.tutorial.entity.task
import org.springframework.stereotype.Component
import org.springframework.data.r2dbc.core.DatabaseClient
import java.util.concurrent.CompletableFuture
import laplacian.tutorial.entity.RepositoryBase
import laplacian.tutorial.entity.user.UserEntity
import laplacian.tutorial.entity.user.UserSearchInput


/**
 * An implementation of the TaskRepository.
 */
@Component
class TaskRepositoryImpl(
    private val db: DatabaseClient
): RepositoryBase(db), TaskRepository {


    /**
     * Finds tasks.
     */
    override fun findTasks(input: TaskSearchInput): CompletableFuture<Set<TaskEntity>> =
        db.execute(
        """
        SELECT
          id
        ${queryOfFindTasks(input)}
        """.trim())
        .let { bindParamsOfFindTasksQuery(it, input) }
        .fetch()
        .all()
        .collectList()
        .map { TaskEntity.fromRecords(it).toSet() }
        .toFuture()

    override fun countTasks(input: TaskSearchInput): CompletableFuture<Long> =
        db
        .execute("SELECT count(*) AS count ${queryOfFindTasks(input)}")
        .let { bindParamsOfFindTasksQuery(it, input) }
        .fetch()
        .first()
        .map{ it["count"] as Long }
        .toFuture()

    fun bindParamsOfFindTasksQuery(
        sql: DatabaseClient.GenericExecuteSpec, input: TaskSearchInput
    ): DatabaseClient.GenericExecuteSpec {
        var result = sql
        result = embedSearchParamsForIntField(result, input.id, "id")
        result = embedSearchParamsForIntField(result, input.userId, "userId")
        result = embedSearchParamsForStringField(result, input.title, "title")
        return result
    }

    fun queryOfFindTasks(input: TaskSearchInput): String = """
        FROM
          t_task
        ${if (input.isEmpty()) "" else """
        WHERE
          ${searchConditionForIntField(input.id, "t_task.id", "id")}
          ${searchConditionForIntField(input.userId, "t_task.user_id", "userId")}
          ${searchConditionForStringField(input.title, "t_task.title", "title")}
        """}
        """
        .trimMargin()
        .replace("""(\n|^)\s*(--.*)?(\n|$)""", "")
        .replace("""AND\s*$""".toRegex(), "")

    /**
     * Loads tasks having the given keys.
     */
    override fun loadTasks(keys: Set<TaskEntity>): CompletableFuture<Set<TaskEntity>> =
        db.execute(
        """
        SELECT
          id,
          user_id,
          title,
          completed
        FROM
          t_task
        WHERE
        -- ${keys.mapIndexed{ index, _ -> """
          t_task.id = :id${index}
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
        .map { TaskEntity.fromRecords(it).toSet() }
        .toFuture()

    /**
     * Loads assignee of this task.
     */
    override fun loadAssigneeOfTask(
        inputs: Map<UserSearchInput, Set<TaskEntity>>
    ): CompletableFuture<Map<Pair<UserSearchInput, TaskEntity>, UserEntity?>> =
        db.execute(
        """
        SELECT
          ${if (inputs.isEmpty()) "" else """
          _condition_.id AS "_condition_id_",
          """}
          t_task.id AS "id",
          t_user.id AS "assignee.id"
        FROM
          ${if (inputs.isEmpty()) "" else """
          (VALUES ${inputs.entries.mapIndexed{ i, _ -> "(${i})"}.joinToString(", ")}) _condition_(id),
          """}
          t_task,
          t_user
        WHERE
          t_task.user_id = t_user.id
        AND (
        ${inputs.entries.mapIndexed{ i, (input, keys) -> """
          (${keys.mapIndexed{ index, key -> """
            t_user.id = ${if (key.userId == null) "null" else ":userId_${i}_${index}"}
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
                    binder = if (key.userId == null) binder else binder.bind("userId_${i}_${index}", key.userId!!)
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
            TaskEntity.fromRecordsGrouping(records) { record ->
                val searchConditionIndex = record["_condition_id_"]
                if (searchConditionIndex == null || searchConditionIndex !is Int) {
                    UserSearchInput()
                }
                else {
                    inputs.keys.toList().getOrElse(searchConditionIndex){ UserSearchInput() }
                }
            }.flatMap { (searchCondition, tasks) ->
                tasks.map { task -> searchCondition to task }
            }
            .map { it to it.second.assignee }
            .toMap()
        }
        .toFuture()

}