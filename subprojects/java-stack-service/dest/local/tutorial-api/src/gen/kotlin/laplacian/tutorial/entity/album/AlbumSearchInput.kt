package laplacian.tutorial.entity.album

import laplacian.tutorial.api.query.*
import laplacian.tutorial.api.util.*

data class AlbumSearchInput (
    val id: IntSearchInput = IntSearchInput(),
    val userId: IntSearchInput = IntSearchInput(),
    val title: StringSearchInput = StringSearchInput()
) {
    fun isEmpty(): Boolean =
        id.isEmpty() &&
        userId.isEmpty() &&
        title.isEmpty()

    companion object {
        fun from(args: Map<String, Any?>): AlbumSearchInput {
            return AlbumSearchInput(
                id = IntSearchInput.from(args["id"]),
                userId = IntSearchInput.from(args["userId"]),
                title = StringSearchInput.from(args["title"])
            )
        }
    }
}