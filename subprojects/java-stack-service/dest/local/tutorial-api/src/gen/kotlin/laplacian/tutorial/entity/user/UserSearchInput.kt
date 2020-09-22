package laplacian.tutorial.entity.user

import laplacian.tutorial.api.query.*
import laplacian.tutorial.api.util.*

data class UserSearchInput (
    val id: IntSearchInput = IntSearchInput(),
    val name: StringSearchInput = StringSearchInput(),
    val username: StringSearchInput = StringSearchInput(),
    val email: StringSearchInput = StringSearchInput(),
    val phone: StringSearchInput = StringSearchInput(),
    val website: StringSearchInput = StringSearchInput()
) {
    fun isEmpty(): Boolean =
        id.isEmpty() &&
        name.isEmpty() &&
        username.isEmpty() &&
        email.isEmpty() &&
        phone.isEmpty() &&
        website.isEmpty()

    companion object {
        fun from(args: Map<String, Any?>): UserSearchInput {
            return UserSearchInput(
                id = IntSearchInput.from(args["id"]),
                name = StringSearchInput.from(args["name"]),
                username = StringSearchInput.from(args["username"]),
                email = StringSearchInput.from(args["email"]),
                phone = StringSearchInput.from(args["phone"]),
                website = StringSearchInput.from(args["website"])
            )
        }
    }
}