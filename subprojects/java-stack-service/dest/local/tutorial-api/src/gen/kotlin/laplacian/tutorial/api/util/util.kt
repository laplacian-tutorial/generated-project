package laplacian.tutorial.api.util

fun <T> Map<*, *>.getList(key: String): List<T> {
    val list = this[key]
    if (list == null || list !is List<*>) return emptyList<T>()
    @Suppress("UNCHECKED_CAST")
    return list as List<T>
}

fun <T> Map<*, *>.getAs(key: String): T? {
    @Suppress("UNCHECKED_CAST")
    return this[key] as T?
}