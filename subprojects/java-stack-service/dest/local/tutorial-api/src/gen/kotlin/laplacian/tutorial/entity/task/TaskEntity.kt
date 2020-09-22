package laplacian.tutorial.entity.task
import laplacian.tutorial.entity.user.UserEntity


import java.util.*

/**
 * task
 */
class TaskEntity {
    /**
     * The id of this task.
     */
    private var _id: Int? = null
    var id: Int
        get() = _id!!
        set(v) { _id = v }
    /**
     * The user_id of this task.
     */
    var userId: Int? = null
    /**
     * The title of this task.
     */
    var title: String = ""

    /**
     * Defines this task is completed or not.
     */
    var completed: Boolean = false


    /**
     * assignee
     */
    var assignee: UserEntity?
        get() = _assignee
        set(value) { _assignee = value }
    private var _assignee: UserEntity? = null

    override fun equals(other: Any?): Boolean =
        (other === this) ||
        (other != null) &&
        (other is TaskEntity) &&
        Objects.equals(this.id, other.id)

    override fun hashCode(): Int = Objects.hash(
        this.id
    )

    override fun toString(): String = "TaskEntity(" +
        "id: $id)"

    companion object {
        fun fromRecord(
            record: Record,
            prefix: String = ""
        ): TaskEntity = TaskEntity().apply {
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
            val nullableInteger = {_: String, value: Any? -> if (value is Int) value else value?.toString()?.toInt()}
            val nonNullBoolean = {key: String, value: Any? ->
                nonNullInteger(key, value) != 0
            }
            record.forEach { k, v ->
                when(k) {
                    prefix + "id" -> id = nonNullInteger(k, v)
                    prefix + "user_id" -> userId = nullableInteger(k, v)
                    prefix + "title" -> title = nonNullString(k, v)
                    prefix + "completed" -> completed = nonNullBoolean(k, v)
                }
            }
        }

        fun <T> fromRecordsGrouping(
            records: Records,
            groupBy: (Record) -> T
        ): Map<T, List<TaskEntity>> {
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
        ): List<TaskEntity> {
            if (records.isEmpty()) return emptyList()
            val nestingAssignee = records.first().keys.any{ it.startsWith("assignee.") }
            return records.fold(mutableMapOf<TaskEntity, MutableList<Record>>()) { acc, record ->
                val key = TaskEntity.fromRecord(record, prefix)
                acc.also {
                    it.getOrPut(key) { mutableListOf<Record>() }
                      .add(record)
                }
            }.map { (task, records) ->
                if (nestingAssignee) {
                    task.assignee = UserEntity.fromRecords(records, "assignee.").first()
                }
                task
            }
        }
    }
}