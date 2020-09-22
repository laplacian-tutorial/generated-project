package laplacian.tutorial.api.query

import laplacian.tutorial.api.util.*

data class IntSearchInput (
    val equalsTo: List<Int> = emptyList(),
    val inRangeFrom: Int? = null,
    val inRangeTo: Int? = null
) {
    fun isEmpty(): Boolean =
        inRangeFrom == null &&
        inRangeTo == null &&
        equalsTo.isEmpty()

    companion object {
        fun from(map: Any?): IntSearchInput =
            if (map == null || map !is Map<*, *>) IntSearchInput()
            else IntSearchInput(
                equalsTo = map.getList<Int>("equalsTo"),
                inRangeFrom = map.getAs<Map<String,*>>("inRange")?.getAs<Int>("from"),
                inRangeTo = map.getAs<Map<String,*>>("inRange")?.getAs<Int>("to")
            )
    }
}