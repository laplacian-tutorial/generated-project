package laplacian.tutorial.entity

import org.springframework.data.r2dbc.core.DatabaseClient
import laplacian.tutorial.api.query.IntSearchInput
import laplacian.tutorial.api.query.StringSearchInput

open class RepositoryBase(
    private val db: DatabaseClient
) {

    fun searchConditionForIntField(
      input: IntSearchInput, columnName: String, fieldName: String
    ): String = """
          ${if (input.equalsTo.isEmpty()) "" else """
          (
          ${input.equalsTo.mapIndexed { index, _ -> """
            ${columnName} = :${fieldName}_equalsTo_${index}
          """}.joinToString("\nOR\n")}
          ) AND
          """}
          ${if (input.inRangeFrom == null) "" else """
          (
            ${columnName} >= :${fieldName}_inRangeFrom
          ) AND
          """}
          ${if (input.inRangeTo == null) "" else """
          (
            ${columnName} <= :${fieldName}_inRangeTo
          ) AND
          """}
    """.trim()

    fun embedSearchParamsForIntField(
        sql: DatabaseClient.GenericExecuteSpec,
        input: IntSearchInput,
        fieldName: String
    ): DatabaseClient.GenericExecuteSpec {
        var result = input.equalsTo.foldIndexed(sql){ index, acc, value ->
            acc.bind("${fieldName}_equalsTo_${index}", value)
        }
        if (input.inRangeFrom != null) {
            result = result.bind("${fieldName}_inRangeFrom", input.inRangeFrom)
        }
        if (input.inRangeTo != null) {
            result = result.bind("${fieldName}_inRangeTo", input.inRangeTo)
        }
        return result
    }

    fun searchConditionForStringField(
      input: StringSearchInput, columnName: String, prefix: String
    ): String = """
          ${if (input.equalsTo.isEmpty()) "" else """
          (
          ${input.equalsTo.mapIndexed { index, _ -> """
            ${columnName} = :${prefix}_equalsTo_${index}
          """}.joinToString("\nOR\n")}
          ) AND
          """}
          ${if (input.startsWith.isEmpty()) "" else """
          (
          ${input.startsWith.mapIndexed { index, _ -> """
            ${columnName} LIKE :${prefix}_startsWith_${index} ESCAPE '\'
          """}.joinToString("\nOR\n")}
          ) AND
          """}
          ${if (input.endsWith.isEmpty()) "" else """
          (
          ${input.endsWith.mapIndexed { index, _ -> """
            ${columnName} LIKE :${prefix}_endsWith_${index} ESCAPE '\'
          """}.joinToString("\nOR\n")}
          ) AND
          """}
          ${if (input.contains.isEmpty()) "" else """
          (
          ${input.contains.mapIndexed { index, _ -> """
            ${columnName} LIKE :${prefix}_contains_${index} ESCAPE '\'
          """}.joinToString("\nOR\n")}
          ) AND
          """}
          ${if (input.inRangeFrom == null) "" else """
          (
            ${columnName} >= :${prefix}_inRangeFrom
          ) AND
          """}
          ${if (input.inRangeTo == null) "" else """
          (
            ${columnName} <= :${prefix}_inRangeTo
          ) AND
          """}
    """.trim()

    fun embedSearchParamsForStringField(
        sql: DatabaseClient.GenericExecuteSpec,
        input: StringSearchInput,
        fieldName: String
    ): DatabaseClient.GenericExecuteSpec {
        var result = input.equalsTo.foldIndexed(sql){ index, acc, value ->
            acc.bind("${fieldName}_equalsTo_${index}", value)
        }
        result = input.startsWith.foldIndexed(result){ index, acc, value ->
            acc.bind("${fieldName}_startsWith_${index}", escapeWildcard(value) + "%")
        }
        result = input.endsWith.foldIndexed(result){ index, acc, value ->
            acc.bind("${fieldName}_endsWith_${index}", "%" + escapeWildcard(value))
        }
        result = input.contains.foldIndexed(result){ index, acc, value ->
            acc.bind("${fieldName}_contains_${index}", "%" + escapeWildcard(value) + "%")
        }
        if (input.inRangeFrom != null) {
            result = result.bind("${fieldName}_inRangeFrom", input.inRangeFrom)
        }
        if (input.inRangeTo != null) {
            result = result.bind("${fieldName}_inRangeTo", input.inRangeTo)
        }
        return result
    }

    fun escapeWildcard(value: String, escapeSequence: String = "\\"): String =
        value
        .replace(escapeSequence, escapeSequence + escapeSequence)
        .replace("%", escapeSequence + "%")
        .replace("_", escapeSequence + "_")
}