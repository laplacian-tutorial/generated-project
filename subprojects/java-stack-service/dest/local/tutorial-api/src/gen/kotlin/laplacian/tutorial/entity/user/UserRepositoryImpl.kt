package laplacian.tutorial.entity.user
import org.springframework.stereotype.Component
import org.springframework.data.r2dbc.core.DatabaseClient
import java.util.concurrent.CompletableFuture
import laplacian.tutorial.entity.RepositoryBase
import laplacian.tutorial.entity.task.TaskEntity
import laplacian.tutorial.entity.task.TaskSearchInput
import laplacian.tutorial.entity.album.AlbumEntity
import laplacian.tutorial.entity.album.AlbumSearchInput
import laplacian.tutorial.entity.post.PostEntity
import laplacian.tutorial.entity.post.PostSearchInput


/**
 * An implementation of the UserRepository.
 */
@Component
class UserRepositoryImpl(
    private val db: DatabaseClient
): RepositoryBase(db), UserRepository {


    /**
     * Finds users.
     */
    override fun findUsers(input: UserSearchInput): CompletableFuture<Set<UserEntity>> =
        db.execute(
        """
        SELECT
          id
        ${queryOfFindUsers(input)}
        """.trim())
        .let { bindParamsOfFindUsersQuery(it, input) }
        .fetch()
        .all()
        .collectList()
        .map { UserEntity.fromRecords(it).toSet() }
        .toFuture()

    override fun countUsers(input: UserSearchInput): CompletableFuture<Long> =
        db
        .execute("SELECT count(*) AS count ${queryOfFindUsers(input)}")
        .let { bindParamsOfFindUsersQuery(it, input) }
        .fetch()
        .first()
        .map{ it["count"] as Long }
        .toFuture()

    fun bindParamsOfFindUsersQuery(
        sql: DatabaseClient.GenericExecuteSpec, input: UserSearchInput
    ): DatabaseClient.GenericExecuteSpec {
        var result = sql
        result = embedSearchParamsForIntField(result, input.id, "id")
        result = embedSearchParamsForStringField(result, input.name, "name")
        result = embedSearchParamsForStringField(result, input.username, "username")
        result = embedSearchParamsForStringField(result, input.email, "email")
        result = embedSearchParamsForStringField(result, input.phone, "phone")
        result = embedSearchParamsForStringField(result, input.website, "website")
        return result
    }

    fun queryOfFindUsers(input: UserSearchInput): String = """
        FROM
          t_user
        ${if (input.isEmpty()) "" else """
        WHERE
          ${searchConditionForIntField(input.id, "t_user.id", "id")}
          ${searchConditionForStringField(input.name, "t_user.name", "name")}
          ${searchConditionForStringField(input.username, "t_user.username", "username")}
          ${searchConditionForStringField(input.email, "t_user.email", "email")}
          ${searchConditionForStringField(input.phone, "t_user.phone", "phone")}
          ${searchConditionForStringField(input.website, "t_user.website", "website")}
        """}
        """
        .trimMargin()
        .replace("""(\n|^)\s*(--.*)?(\n|$)""", "")
        .replace("""AND\s*$""".toRegex(), "")

    /**
     * Loads users having the given keys.
     */
    override fun loadUsers(keys: Set<UserEntity>): CompletableFuture<Set<UserEntity>> =
        db.execute(
        """
        SELECT
          id,
          name,
          username,
          email,
          phone,
          website
        FROM
          t_user
        WHERE
        -- ${keys.mapIndexed{ index, _ -> """
          t_user.id = :id${index}
        -- """}.joinToString("\nOR\n")}
        """.trimMargin())
        .let { sql ->
            keys.foldIndexed(sql){ index, acc, key ->
                var binder = acc
                binder = binder.bind("id${index}", key.id)
                binder
            }
        }
        .fetch()
        .all()
        .collectList()
        .map { UserEntity.fromRecords(it).toSet() }
        .toFuture()

    /**
     * Loads address of this user.
     */
    override fun loadAddressOfUser(
        inputs: Map<AddressSearchInput, Set<UserEntity>>
    ): CompletableFuture<Map<Pair<AddressSearchInput, UserEntity>, AddressEntity>> =
        db.execute(
        """
        SELECT
          ${if (inputs.isEmpty()) "" else """
          _condition_.id AS "_condition_id_",
          """}
          t_user.id AS "id",
          t_address.user_id AS "address.user_id"
        FROM
          ${if (inputs.isEmpty()) "" else """
          (VALUES ${inputs.entries.mapIndexed{ i, _ -> "(${i})"}.joinToString(", ")}) _condition_(id),
          """}
          t_user,
          t_address
        WHERE
          t_user.id = t_address.user_id
        AND (
        ${inputs.entries.mapIndexed{ i, (input, keys) -> """
          (${keys.mapIndexed{ index, _ -> """
            t_address.user_id = :userId_${i}_${index}
          -- """}.joinToString("\nOR\n")}
          )
          AND
            ${if (input.isEmpty()) "" else """
            ${searchConditionForStringField(input.street, "t_address.street", "street_${i}_")}
            ${searchConditionForStringField(input.suite, "t_address.suite", "suite_${i}_")}
            ${searchConditionForStringField(input.city, "t_address.city", "city_${i}_")}
            ${searchConditionForStringField(input.zipcode, "t_address.zipcode", "zipcode_${i}_")}
            ${searchConditionForStringField(input.latitude, "t_address.latitude", "latitude_${i}_")}
            ${searchConditionForStringField(input.longitude, "t_address.longitude", "longitude_${i}_")}
            """}
             _condition_.id = ${i}
        """}.joinToString("\nOR\n")}
         )
        """
        .trimMargin()
        .replace("""(\n|^)\s*(--.*)?(\n|$)""", "")
        .replace("""AND\s*$""".toRegex(), ""))
        .let {
            var sql = it
            inputs.entries.mapIndexed{ i, (input, keys) ->
                sql = keys.foldIndexed(sql) { index, acc, key ->
                    var binder = acc
                    binder = binder.bind("userId_${i}_${index}", key.id)
                    binder
                }
                sql = embedSearchParamsForStringField(sql, input.street, "street_${i}_")
                sql = embedSearchParamsForStringField(sql, input.suite, "suite_${i}_")
                sql = embedSearchParamsForStringField(sql, input.city, "city_${i}_")
                sql = embedSearchParamsForStringField(sql, input.zipcode, "zipcode_${i}_")
                sql = embedSearchParamsForStringField(sql, input.latitude, "latitude_${i}_")
                sql = embedSearchParamsForStringField(sql, input.longitude, "longitude_${i}_")
            }
            sql
        }
        .fetch()
        .all()
        .collectList()
        .map { records ->
            UserEntity.fromRecordsGrouping(records) { record ->
                val searchConditionIndex = record["_condition_id_"]
                if (searchConditionIndex == null || searchConditionIndex !is Int) {
                    AddressSearchInput()
                }
                else {
                    inputs.keys.toList().getOrElse(searchConditionIndex){ AddressSearchInput() }
                }
            }.flatMap { (searchCondition, users) ->
                users.map { user -> searchCondition to user }
            }
            .map { it to it.second.address }
            .toMap()
        }
        .toFuture()
    /**
     * Loads company of this user.
     */
    override fun loadCompanyOfUser(
        inputs: Map<CompanySearchInput, Set<UserEntity>>
    ): CompletableFuture<Map<Pair<CompanySearchInput, UserEntity>, CompanyEntity?>> =
        db.execute(
        """
        SELECT
          ${if (inputs.isEmpty()) "" else """
          _condition_.id AS "_condition_id_",
          """}
          t_user.id AS "id",
          t_company.user_id AS "company.user_id"
        FROM
          ${if (inputs.isEmpty()) "" else """
          (VALUES ${inputs.entries.mapIndexed{ i, _ -> "(${i})"}.joinToString(", ")}) _condition_(id),
          """}
          t_user,
          t_company
        WHERE
          t_user.id = t_company.user_id
        AND (
        ${inputs.entries.mapIndexed{ i, (input, keys) -> """
          (${keys.mapIndexed{ index, _ -> """
            t_company.user_id = :userId_${i}_${index}
          -- """}.joinToString("\nOR\n")}
          )
          AND
            ${if (input.isEmpty()) "" else """
            ${searchConditionForStringField(input.name, "t_company.name", "name_${i}_")}
            ${searchConditionForStringField(input.catchPhrase, "t_company.catch_phrase", "catchPhrase_${i}_")}
            ${searchConditionForStringField(input.bs, "t_company.bs", "bs_${i}_")}
            """}
             _condition_.id = ${i}
        """}.joinToString("\nOR\n")}
         )
        """
        .trimMargin()
        .replace("""(\n|^)\s*(--.*)?(\n|$)""", "")
        .replace("""AND\s*$""".toRegex(), ""))
        .let {
            var sql = it
            inputs.entries.mapIndexed{ i, (input, keys) ->
                sql = keys.foldIndexed(sql) { index, acc, key ->
                    var binder = acc
                    binder = binder.bind("userId_${i}_${index}", key.id)
                    binder
                }
                sql = embedSearchParamsForStringField(sql, input.name, "name_${i}_")
                sql = embedSearchParamsForStringField(sql, input.catchPhrase, "catchPhrase_${i}_")
                sql = embedSearchParamsForStringField(sql, input.bs, "bs_${i}_")
            }
            sql
        }
        .fetch()
        .all()
        .collectList()
        .map { records ->
            UserEntity.fromRecordsGrouping(records) { record ->
                val searchConditionIndex = record["_condition_id_"]
                if (searchConditionIndex == null || searchConditionIndex !is Int) {
                    CompanySearchInput()
                }
                else {
                    inputs.keys.toList().getOrElse(searchConditionIndex){ CompanySearchInput() }
                }
            }.flatMap { (searchCondition, users) ->
                users.map { user -> searchCondition to user }
            }
            .map { it to it.second.company }
            .toMap()
        }
        .toFuture()
    /**
     * Loads tasks of this user.
     */
    override fun loadTasksOfUser(
        inputs: Map<TaskSearchInput, Set<UserEntity>>
    ): CompletableFuture<Map<Pair<TaskSearchInput, UserEntity>, List<TaskEntity>>> =
        db.execute(
        """
        SELECT
          ${if (inputs.isEmpty()) "" else """
          _condition_.id AS "_condition_id_",
          """}
          t_user.id AS "id",
          t_task.id AS "tasks.id"
        FROM
          ${if (inputs.isEmpty()) "" else """
          (VALUES ${inputs.entries.mapIndexed{ i, _ -> "(${i})"}.joinToString(", ")}) _condition_(id),
          """}
          t_user,
          t_task
        WHERE
          t_user.id = t_task.user_id
        AND (
        ${inputs.entries.mapIndexed{ i, (input, keys) -> """
          (${keys.mapIndexed{ index, _ -> """
            t_task.user_id = :id_${i}_${index}
          -- """}.joinToString("\nOR\n")}
          )
          AND
            ${if (input.isEmpty()) "" else """
            ${searchConditionForIntField(input.id, "t_task.id", "id_${i}_")}
            ${searchConditionForIntField(input.userId, "t_task.user_id", "userId_${i}_")}
            ${searchConditionForStringField(input.title, "t_task.title", "title_${i}_")}
            """}
             _condition_.id = ${i}
        """}.joinToString("\nOR\n")}
         )
        """
        .trimMargin()
        .replace("""(\n|^)\s*(--.*)?(\n|$)""", "")
        .replace("""AND\s*$""".toRegex(), ""))
        .let {
            var sql = it
            inputs.entries.mapIndexed{ i, (input, keys) ->
                sql = keys.foldIndexed(sql) { index, acc, key ->
                    var binder = acc
                    binder = binder.bind("id_${i}_${index}", key.id)
                    binder
                }
                sql = embedSearchParamsForIntField(sql, input.id, "id_${i}_")
                sql = embedSearchParamsForIntField(sql, input.userId, "userId_${i}_")
                sql = embedSearchParamsForStringField(sql, input.title, "title_${i}_")
            }
            sql
        }
        .fetch()
        .all()
        .collectList()
        .map { records ->
            UserEntity.fromRecordsGrouping(records) { record ->
                val searchConditionIndex = record["_condition_id_"]
                if (searchConditionIndex == null || searchConditionIndex !is Int) {
                    TaskSearchInput()
                }
                else {
                    inputs.keys.toList().getOrElse(searchConditionIndex){ TaskSearchInput() }
                }
            }.flatMap { (searchCondition, users) ->
                users.map { user -> searchCondition to user }
            }
            .map { it to it.second.tasks }
            .toMap()
        }
        .toFuture()
    /**
     * Loads albums of this user.
     */
    override fun loadAlbumsOfUser(
        inputs: Map<AlbumSearchInput, Set<UserEntity>>
    ): CompletableFuture<Map<Pair<AlbumSearchInput, UserEntity>, List<AlbumEntity>>> =
        db.execute(
        """
        SELECT
          ${if (inputs.isEmpty()) "" else """
          _condition_.id AS "_condition_id_",
          """}
          t_user.id AS "id",
          t_album.id AS "albums.id"
        FROM
          ${if (inputs.isEmpty()) "" else """
          (VALUES ${inputs.entries.mapIndexed{ i, _ -> "(${i})"}.joinToString(", ")}) _condition_(id),
          """}
          t_user,
          t_album
        WHERE
          t_user.id = t_album.user_id
        AND (
        ${inputs.entries.mapIndexed{ i, (input, keys) -> """
          (${keys.mapIndexed{ index, _ -> """
            t_album.user_id = :id_${i}_${index}
          -- """}.joinToString("\nOR\n")}
          )
          AND
            ${if (input.isEmpty()) "" else """
            ${searchConditionForIntField(input.id, "t_album.id", "id_${i}_")}
            ${searchConditionForIntField(input.userId, "t_album.user_id", "userId_${i}_")}
            ${searchConditionForStringField(input.title, "t_album.title", "title_${i}_")}
            """}
             _condition_.id = ${i}
        """}.joinToString("\nOR\n")}
         )
        """
        .trimMargin()
        .replace("""(\n|^)\s*(--.*)?(\n|$)""", "")
        .replace("""AND\s*$""".toRegex(), ""))
        .let {
            var sql = it
            inputs.entries.mapIndexed{ i, (input, keys) ->
                sql = keys.foldIndexed(sql) { index, acc, key ->
                    var binder = acc
                    binder = binder.bind("id_${i}_${index}", key.id)
                    binder
                }
                sql = embedSearchParamsForIntField(sql, input.id, "id_${i}_")
                sql = embedSearchParamsForIntField(sql, input.userId, "userId_${i}_")
                sql = embedSearchParamsForStringField(sql, input.title, "title_${i}_")
            }
            sql
        }
        .fetch()
        .all()
        .collectList()
        .map { records ->
            UserEntity.fromRecordsGrouping(records) { record ->
                val searchConditionIndex = record["_condition_id_"]
                if (searchConditionIndex == null || searchConditionIndex !is Int) {
                    AlbumSearchInput()
                }
                else {
                    inputs.keys.toList().getOrElse(searchConditionIndex){ AlbumSearchInput() }
                }
            }.flatMap { (searchCondition, users) ->
                users.map { user -> searchCondition to user }
            }
            .map { it to it.second.albums }
            .toMap()
        }
        .toFuture()
    /**
     * Loads posts of this user.
     */
    override fun loadPostsOfUser(
        inputs: Map<PostSearchInput, Set<UserEntity>>
    ): CompletableFuture<Map<Pair<PostSearchInput, UserEntity>, List<PostEntity>>> =
        db.execute(
        """
        SELECT
          ${if (inputs.isEmpty()) "" else """
          _condition_.id AS "_condition_id_",
          """}
          t_user.id AS "id",
          t_post.id AS "posts.id"
        FROM
          ${if (inputs.isEmpty()) "" else """
          (VALUES ${inputs.entries.mapIndexed{ i, _ -> "(${i})"}.joinToString(", ")}) _condition_(id),
          """}
          t_user,
          t_post
        WHERE
          t_user.id = t_post.user_id
        AND (
        ${inputs.entries.mapIndexed{ i, (input, keys) -> """
          (${keys.mapIndexed{ index, _ -> """
            t_post.user_id = :id_${i}_${index}
          -- """}.joinToString("\nOR\n")}
          )
          AND
            ${if (input.isEmpty()) "" else """
            ${searchConditionForIntField(input.id, "t_post.id", "id_${i}_")}
            ${searchConditionForIntField(input.userId, "t_post.user_id", "userId_${i}_")}
            ${searchConditionForStringField(input.title, "t_post.title", "title_${i}_")}
            ${searchConditionForStringField(input.body, "t_post.body", "body_${i}_")}
            """}
             _condition_.id = ${i}
        """}.joinToString("\nOR\n")}
         )
        """
        .trimMargin()
        .replace("""(\n|^)\s*(--.*)?(\n|$)""", "")
        .replace("""AND\s*$""".toRegex(), ""))
        .let {
            var sql = it
            inputs.entries.mapIndexed{ i, (input, keys) ->
                sql = keys.foldIndexed(sql) { index, acc, key ->
                    var binder = acc
                    binder = binder.bind("id_${i}_${index}", key.id)
                    binder
                }
                sql = embedSearchParamsForIntField(sql, input.id, "id_${i}_")
                sql = embedSearchParamsForIntField(sql, input.userId, "userId_${i}_")
                sql = embedSearchParamsForStringField(sql, input.title, "title_${i}_")
                sql = embedSearchParamsForStringField(sql, input.body, "body_${i}_")
            }
            sql
        }
        .fetch()
        .all()
        .collectList()
        .map { records ->
            UserEntity.fromRecordsGrouping(records) { record ->
                val searchConditionIndex = record["_condition_id_"]
                if (searchConditionIndex == null || searchConditionIndex !is Int) {
                    PostSearchInput()
                }
                else {
                    inputs.keys.toList().getOrElse(searchConditionIndex){ PostSearchInput() }
                }
            }.flatMap { (searchCondition, users) ->
                users.map { user -> searchCondition to user }
            }
            .map { it to it.second.posts }
            .toMap()
        }
        .toFuture()


    /**
     * Finds addresses.
     */
    override fun findAddresses(input: AddressSearchInput): CompletableFuture<Set<AddressEntity>> =
        db.execute(
        """
        SELECT
          user_id,
        ${queryOfFindAddresses(input)}
        """.trim())
        .let { bindParamsOfFindAddressesQuery(it, input) }
        .fetch()
        .all()
        .collectList()
        .map { AddressEntity.fromRecords(it).toSet() }
        .toFuture()

    override fun countAddresses(input: AddressSearchInput): CompletableFuture<Long> =
        db
        .execute("SELECT count(*) AS count ${queryOfFindAddresses(input)}")
        .let { bindParamsOfFindAddressesQuery(it, input) }
        .fetch()
        .first()
        .map{ it["count"] as Long }
        .toFuture()

    fun bindParamsOfFindAddressesQuery(
        sql: DatabaseClient.GenericExecuteSpec, input: AddressSearchInput
    ): DatabaseClient.GenericExecuteSpec {
        var result = sql
        result = embedSearchParamsForStringField(result, input.street, "street")
        result = embedSearchParamsForStringField(result, input.suite, "suite")
        result = embedSearchParamsForStringField(result, input.city, "city")
        result = embedSearchParamsForStringField(result, input.zipcode, "zipcode")
        result = embedSearchParamsForStringField(result, input.latitude, "latitude")
        result = embedSearchParamsForStringField(result, input.longitude, "longitude")
        return result
    }

    fun queryOfFindAddresses(input: AddressSearchInput): String = """
        FROM
          t_address
        ${if (input.isEmpty()) "" else """
        WHERE
          ${searchConditionForStringField(input.street, "t_address.street", "street")}
          ${searchConditionForStringField(input.suite, "t_address.suite", "suite")}
          ${searchConditionForStringField(input.city, "t_address.city", "city")}
          ${searchConditionForStringField(input.zipcode, "t_address.zipcode", "zipcode")}
          ${searchConditionForStringField(input.latitude, "t_address.latitude", "latitude")}
          ${searchConditionForStringField(input.longitude, "t_address.longitude", "longitude")}
        """}
        """
        .trimMargin()
        .replace("""(\n|^)\s*(--.*)?(\n|$)""", "")
        .replace("""AND\s*$""".toRegex(), "")

    /**
     * Loads addresses having the given keys.
     */
    override fun loadAddresses(keys: Set<AddressEntity>): CompletableFuture<Set<AddressEntity>> =
        db.execute(
        """
        SELECT
          user_id,
          street,
          suite,
          city,
          zipcode,
          latitude,
          longitude
        FROM
          t_address
        WHERE
        -- ${keys.mapIndexed{ index, _ -> """
          t_address.user_id = :userId${index}
        -- """}.joinToString("\nOR\n")}
        """.trimMargin())
        .let { sql ->
            keys.foldIndexed(sql){ index, acc, key ->
                var binder = acc
                binder = binder.bind("userId${index}", key.userId)
                binder
            }
        }
        .fetch()
        .all()
        .collectList()
        .map { AddressEntity.fromRecords(it).toSet() }
        .toFuture()



    /**
     * Finds companies.
     */
    override fun findCompanies(input: CompanySearchInput): CompletableFuture<Set<CompanyEntity>> =
        db.execute(
        """
        SELECT
          user_id,
        ${queryOfFindCompanies(input)}
        """.trim())
        .let { bindParamsOfFindCompaniesQuery(it, input) }
        .fetch()
        .all()
        .collectList()
        .map { CompanyEntity.fromRecords(it).toSet() }
        .toFuture()

    override fun countCompanies(input: CompanySearchInput): CompletableFuture<Long> =
        db
        .execute("SELECT count(*) AS count ${queryOfFindCompanies(input)}")
        .let { bindParamsOfFindCompaniesQuery(it, input) }
        .fetch()
        .first()
        .map{ it["count"] as Long }
        .toFuture()

    fun bindParamsOfFindCompaniesQuery(
        sql: DatabaseClient.GenericExecuteSpec, input: CompanySearchInput
    ): DatabaseClient.GenericExecuteSpec {
        var result = sql
        result = embedSearchParamsForStringField(result, input.name, "name")
        result = embedSearchParamsForStringField(result, input.catchPhrase, "catchPhrase")
        result = embedSearchParamsForStringField(result, input.bs, "bs")
        return result
    }

    fun queryOfFindCompanies(input: CompanySearchInput): String = """
        FROM
          t_company
        ${if (input.isEmpty()) "" else """
        WHERE
          ${searchConditionForStringField(input.name, "t_company.name", "name")}
          ${searchConditionForStringField(input.catchPhrase, "t_company.catch_phrase", "catchPhrase")}
          ${searchConditionForStringField(input.bs, "t_company.bs", "bs")}
        """}
        """
        .trimMargin()
        .replace("""(\n|^)\s*(--.*)?(\n|$)""", "")
        .replace("""AND\s*$""".toRegex(), "")

    /**
     * Loads companies having the given keys.
     */
    override fun loadCompanies(keys: Set<CompanyEntity>): CompletableFuture<Set<CompanyEntity>> =
        db.execute(
        """
        SELECT
          user_id,
          name,
          catch_phrase,
          bs
        FROM
          t_company
        WHERE
        -- ${keys.mapIndexed{ index, _ -> """
          t_company.user_id = :userId${index}
        -- """}.joinToString("\nOR\n")}
        """.trimMargin())
        .let { sql ->
            keys.foldIndexed(sql){ index, acc, key ->
                var binder = acc
                binder = binder.bind("userId${index}", key.userId)
                binder
            }
        }
        .fetch()
        .all()
        .collectList()
        .map { CompanyEntity.fromRecords(it).toSet() }
        .toFuture()


}