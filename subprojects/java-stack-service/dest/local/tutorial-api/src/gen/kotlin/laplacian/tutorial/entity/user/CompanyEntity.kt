package laplacian.tutorial.entity.user


import java.util.*

/**
 * company
 */
class CompanyEntity {
    /**
     * The id of this user.
     */
    private var _userId: Int? = null
    var userId: Int
        get() = _userId!!
        set(v) { _userId = v }
    /**
     * The name of this company.
     */
    lateinit var name: String
    /**
     * The catch_phrase of this company.
     */
    var catchPhrase: String? = null
    /**
     * The bs of this company.
     */
    var bs: String? = null


    override fun equals(other: Any?): Boolean =
        (other === this) ||
        (other != null) &&
        (other is CompanyEntity) &&
        Objects.equals(this.userId, other.userId)

    override fun hashCode(): Int = Objects.hash(
        this.userId
    )

    override fun toString(): String = "CompanyEntity(" +
        "userId: $userId)"

    companion object {
        fun fromRecord(
            record: Record,
            prefix: String = ""
        ): CompanyEntity = CompanyEntity().apply {
            val nonNullString = {key: String, value: Any? ->
                value?.toString() ?: throw IllegalArgumentException(
                    "$key must not be null: $record"
                )
            }
            val nullableString = {_: String, value: Any? -> value?.toString()}
            val nonNullInteger = {key: String, value: Any? ->
                if (value is Int)
                  value
                else
                  value?.toString()?.toInt() ?: throw IllegalArgumentException(
                    "$key must not be null: $record"
                  )
            }
            record.forEach { k, v ->
                when(k) {
                    prefix + "user_id" -> userId = nonNullInteger(k, v)
                    prefix + "name" -> name = nonNullString(k, v)
                    prefix + "catch_phrase" -> catchPhrase = nullableString(k, v)
                    prefix + "bs" -> bs = nullableString(k, v)
                }
            }
        }

        fun <T> fromRecordsGrouping(
            records: Records,
            groupBy: (Record) -> T
        ): Map<T, List<CompanyEntity>> {
            if (records.isEmpty()) return emptyMap()
            return records.fold(mutableMapOf<T, MutableList<Record>>()) { acc, record ->
                val key = groupBy(record)
                acc.getOrPut(key) { mutableListOf<Record>() }
                   .add(record)
                acc
            }.mapValues { (_, records) ->
                fromRecords(records)
            }
        }

        fun fromRecords(
            records: Records,
            prefix: String = ""
        ): List<CompanyEntity> {
            return records.map { fromRecord(it, prefix) }
        }

    }
}