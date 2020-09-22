package laplacian.tutorial.entity.task
import laplacian.tutorial.entity.user.UserEntity
import laplacian.tutorial.entity.user.UserSearchInput

import org.dataloader.DataLoader
import org.dataloader.DataLoaderRegistry
import org.springframework.stereotype.Component
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.TypeRuntimeWiring
import graphql.schema.DataFetchingEnvironment
import java.util.concurrent.CompletableFuture

/**
 * An reactive implementation of the task entity query resolver.
 */
@Component
class TaskResolver(
    private val taskRepository: TaskRepository
) {

    /**
     * Loads the user_id of this task.
     */
    fun userId(self: TaskEntity, context: DataFetchingEnvironment): CompletableFuture<Int> =
        context
        .getDataLoader<TaskEntity, TaskEntity>(BY_PK)
        .load(self)
        .thenApply<Int>{ it.userId }
    /**
     * Loads the title of this task.
     */
    fun title(self: TaskEntity, context: DataFetchingEnvironment): CompletableFuture<String> =
        context
        .getDataLoader<TaskEntity, TaskEntity>(BY_PK)
        .load(self)
        .thenApply<String>{ it.title }
    /**
     * Loads the completed of this task.
     */
    fun completed(self: TaskEntity, context: DataFetchingEnvironment): CompletableFuture<Boolean> =
        context
        .getDataLoader<TaskEntity, TaskEntity>(BY_PK)
        .load(self)
        .thenApply<Boolean>{ it.completed }

    /**
     * Loads the assignee of this task.
     */
    fun assignee(self: TaskEntity, context: DataFetchingEnvironment): CompletableFuture<UserEntity> =
        context
        .getDataLoader<Pair<UserSearchInput, TaskEntity>, UserEntity>(ASSIGNEE)
        .load(UserSearchInput.from(context.arguments) to self)

    fun registerLoader(registry: DataLoaderRegistry) {
        registry.register(BY_PK, DataLoader.newMappedDataLoader<TaskEntity, TaskEntity> { keys ->
            taskRepository
            .loadTasks(keys)
            .thenApply{ set -> set.map{ (it to it) }.toMap() }
        })
        registry.register(ASSIGNEE, DataLoader.newMappedDataLoader<Pair<UserSearchInput, TaskEntity>, UserEntity> { entries ->
            val input = entries.fold(mutableMapOf<UserSearchInput, MutableSet<TaskEntity>>()) { acc, (userSearchInput, task) ->
                val tasks = acc.getOrPut(userSearchInput) { mutableSetOf<TaskEntity>() }
                tasks.add(task)
                acc
            }
            taskRepository
            .loadAssigneeOfTask(input)
        })
    }

    fun registerFetcher(wiring: RuntimeWiring.Builder) = wiring.type(
        TypeRuntimeWiring.newTypeWiring("Task")
        .dataFetcher("userId") { env ->
            val key = env.getSource() as TaskEntity
            userId(key, env)
        }
        .dataFetcher("title") { env ->
            val key = env.getSource() as TaskEntity
            title(key, env)
        }
        .dataFetcher("completed") { env ->
            val key = env.getSource() as TaskEntity
            completed(key, env)
        }
        .dataFetcher("assignee") { env ->
            val key = env.getSource() as TaskEntity
            assignee(key, env)
        }
    )

    companion object {
        const val BY_PK = "task_byPK"
        const val ASSIGNEE = "task_assignee"
    }
}