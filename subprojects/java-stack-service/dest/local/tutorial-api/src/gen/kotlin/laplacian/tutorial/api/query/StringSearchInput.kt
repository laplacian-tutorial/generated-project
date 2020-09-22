package laplacian.tutorial.api.query

import laplacian.tutorial.api.util.*

data class StringSearchInput (
    val startsWith: List<String> = emptyList(),
    val endsWith: List<String> = emptyList(),
    val contains: List<String> = emptyList(),
    val equalsTo: List<String> = emptyList(),
    val inRangeFrom: String? = null,
    val inRangeTo: String? = null
) {
    fun isEmpty(): Boolean =
        startsWith.isEmpty() &&
        endsWith.isEmpty() &&
        contains.isEmpty() &&
        inRangeFrom == null &&
        inRangeTo == null &&
        equalsTo.isEmpty()

    companion object {
        fun from(map: Any?): StringSearchInput =
            if (map == null || map !is Map<*, *>) StringSearchInput()
            else StringSearchInput(
                startsWith = map.getList<String>("startsWith"),
                endsWith =  map.getList<String>("endsWith"),
                contains = map.getList<String>("contains"),
                equalsTo = map.getList<String>("equalsTo"),
                inRangeFrom = map.getAs<Map<String,*>>("inRange")?.getAs<String>("from"),
                inRangeTo = map.getAs<Map<String,*>>("inRange")?.getAs<String>("to")
            )
    }
}