package laplacian.tutorial.entity.user


import java.util.*

/**
 * address
 */
class AddressEntity {
    /**
     * The id of this user.
     */
    private var _userId: Int? = null
    var userId: Int
        get() = _userId!!
        set(v) { _userId = v }
    /**
     * The street of this address.
     */
    lateinit var street: String
    /**
     * The suite of this address.
     */
    lateinit var suite: String
    /**
     * The city of this address.
     */
    lateinit var city: String
    /**
     * The zipcode of this address.
     */
    lateinit var zipcode: String
    /**
     * The latitude of this address.
     */
    lateinit var latitude: String
    /**
     * The longitude of this address.
     */
    lateinit var longitude: String


    override fun equals(other: Any?): Boolean =
        (other === this) ||
        (other != null) &&
        (other is AddressEntity) &&
        Objects.equals(this.userId, other.userId)

    override fun hashCode(): Int = Objects.hash(
        this.userId
    )

    override fun toString(): String = "AddressEntity(" +
        "userId: $userId)"

    companion object {
        fun fromRecord(
            record: Record,
            prefix: String = ""
        ): AddressEntity = AddressEntity().apply {
            val nonNullString = {key: String, value: Any? ->
                value?.toString() ?: throw IllegalArgumentException(
                    "$key must not be null: $record"
                )
            }
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
                    prefix + "street" -> street = nonNullString(k, v)
                    prefix + "suite" -> suite = nonNullString(k, v)
                    prefix + "city" -> city = nonNullString(k, v)
                    prefix + "zipcode" -> zipcode = nonNullString(k, v)
                    prefix + "latitude" -> latitude = nonNullString(k, v)
                    prefix + "longitude" -> longitude = nonNullString(k, v)
                }
            }
        }

        fun <T> fromRecordsGrouping(
            records: Records,
            groupBy: (Record) -> T
        ): Map<T, List<AddressEntity>> {
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
        ): List<AddressEntity> {
            return records.map { fromRecord(it, prefix) }
        }

    }
}