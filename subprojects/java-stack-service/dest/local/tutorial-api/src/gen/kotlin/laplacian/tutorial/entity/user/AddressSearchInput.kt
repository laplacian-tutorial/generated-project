package laplacian.tutorial.entity.user

import laplacian.tutorial.api.query.*
import laplacian.tutorial.api.util.*

data class AddressSearchInput (
    val street: StringSearchInput = StringSearchInput(),
    val suite: StringSearchInput = StringSearchInput(),
    val city: StringSearchInput = StringSearchInput(),
    val zipcode: StringSearchInput = StringSearchInput(),
    val latitude: StringSearchInput = StringSearchInput(),
    val longitude: StringSearchInput = StringSearchInput()
) {
    fun isEmpty(): Boolean =
        street.isEmpty() &&
        suite.isEmpty() &&
        city.isEmpty() &&
        zipcode.isEmpty() &&
        latitude.isEmpty() &&
        longitude.isEmpty()

    companion object {
        fun from(args: Map<String, Any?>): AddressSearchInput {
            return AddressSearchInput(
                street = StringSearchInput.from(args["street"]),
                suite = StringSearchInput.from(args["suite"]),
                city = StringSearchInput.from(args["city"]),
                zipcode = StringSearchInput.from(args["zipcode"]),
                latitude = StringSearchInput.from(args["latitude"]),
                longitude = StringSearchInput.from(args["longitude"])
            )
        }
    }
}