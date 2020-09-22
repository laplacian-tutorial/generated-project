package laplacian.tutorial.entity.photo

import laplacian.tutorial.api.query.*
import laplacian.tutorial.api.util.*

data class PhotoSearchInput (
    val id: IntSearchInput = IntSearchInput(),
    val albumId: IntSearchInput = IntSearchInput(),
    val title: StringSearchInput = StringSearchInput(),
    val url: StringSearchInput = StringSearchInput(),
    val thumbnailUrl: StringSearchInput = StringSearchInput(),
    val dateTaken: StringSearchInput = StringSearchInput()
) {
    fun isEmpty(): Boolean =
        id.isEmpty() &&
        albumId.isEmpty() &&
        title.isEmpty() &&
        url.isEmpty() &&
        thumbnailUrl.isEmpty() &&
        dateTaken.isEmpty()

    companion object {
        fun from(args: Map<String, Any?>): PhotoSearchInput {
            return PhotoSearchInput(
                id = IntSearchInput.from(args["id"]),
                albumId = IntSearchInput.from(args["albumId"]),
                title = StringSearchInput.from(args["title"]),
                url = StringSearchInput.from(args["url"]),
                thumbnailUrl = StringSearchInput.from(args["thumbnailUrl"]),
                dateTaken = StringSearchInput.from(args["dateTaken"])
            )
        }
    }
}