package laplacian.tutorial.entity.task

import laplacian.tutorial.api.query.*
import laplacian.tutorial.api.util.*

data class TaskSearchInput (
    val id: IntSearchInput = IntSearchInput(),
    val userId: IntSearchInput = IntSearchInput(),
    val title: StringSearchInput = StringSearchInput(),
    val completed: Boolean? = null
) {
    fun isEmpty(): Boolean =
        id.isEmpty() &&
        userId.isEmpty() &&
        title.isEmpty() &&
        completed == null

    companion object {
        fun from(args: Map<String, Any?>): TaskSearchInput {
            return TaskSearchInput(
                id = IntSearchInput.from(args["id"]),
                userId = IntSearchInput.from(args["userId"]),
                title = StringSearchInput.from(args["title"]),
                completed = args.getAs<Boolean>("completed")
            )
        }
    }
}