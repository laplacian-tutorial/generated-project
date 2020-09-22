package laplacian.tutorial.entity.user

import laplacian.tutorial.api.query.*
import laplacian.tutorial.api.util.*

data class CompanySearchInput (
    val name: StringSearchInput = StringSearchInput(),
    val catchPhrase: StringSearchInput = StringSearchInput(),
    val bs: StringSearchInput = StringSearchInput()
) {
    fun isEmpty(): Boolean =
        name.isEmpty() &&
        catchPhrase.isEmpty() &&
        bs.isEmpty()

    companion object {
        fun from(args: Map<String, Any?>): CompanySearchInput {
            return CompanySearchInput(
                name = StringSearchInput.from(args["name"]),
                catchPhrase = StringSearchInput.from(args["catchPhrase"]),
                bs = StringSearchInput.from(args["bs"])
            )
        }
    }
}