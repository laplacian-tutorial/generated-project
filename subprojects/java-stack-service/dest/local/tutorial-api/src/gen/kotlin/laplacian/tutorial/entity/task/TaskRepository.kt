package laplacian.tutorial.entity.task
import java.util.concurrent.CompletableFuture
import laplacian.tutorial.entity.user.UserEntity
import laplacian.tutorial.entity.user.UserSearchInput


typealias Records = List<Record>
typealias Record = Map<String, Any?>

/**
 * The interface which abstracts the details of the data access concerning tasks.
 */
interface TaskRepository {


    /**
     * Finds tasks which matches the given conditions.
     */
    fun findTasks(input: TaskSearchInput = TaskSearchInput()): CompletableFuture<Set<TaskEntity>>

    /**
     * Counts the number of tasks which matches the given conditions.
     */
    fun countTasks(input: TaskSearchInput = TaskSearchInput()): CompletableFuture<Long>

    /**
     * Loads tasks having given primary keys.
     */
    fun loadTasks(keys: Set<TaskEntity>): CompletableFuture<Set<TaskEntity>>

    /**
     * Loads assignee
     */
    fun loadAssigneeOfTask(inputs: Map<UserSearchInput, Set<TaskEntity>>): CompletableFuture<Map<Pair<UserSearchInput, TaskEntity>, UserEntity?>>

}