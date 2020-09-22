package laplacian.tutorial.entity.user
import java.util.concurrent.CompletableFuture
import laplacian.tutorial.entity.task.TaskEntity
import laplacian.tutorial.entity.task.TaskSearchInput
import laplacian.tutorial.entity.album.AlbumEntity
import laplacian.tutorial.entity.album.AlbumSearchInput
import laplacian.tutorial.entity.post.PostEntity
import laplacian.tutorial.entity.post.PostSearchInput


typealias Records = List<Record>
typealias Record = Map<String, Any?>

/**
 * The interface which abstracts the details of the data access concerning users.
 */
interface UserRepository {


    /**
     * Finds users which matches the given conditions.
     */
    fun findUsers(input: UserSearchInput = UserSearchInput()): CompletableFuture<Set<UserEntity>>

    /**
     * Counts the number of users which matches the given conditions.
     */
    fun countUsers(input: UserSearchInput = UserSearchInput()): CompletableFuture<Long>

    /**
     * Loads users having given primary keys.
     */
    fun loadUsers(keys: Set<UserEntity>): CompletableFuture<Set<UserEntity>>

    /**
     * Loads address
     */
    fun loadAddressOfUser(inputs: Map<AddressSearchInput, Set<UserEntity>>): CompletableFuture<Map<Pair<AddressSearchInput, UserEntity>, AddressEntity>>
    /**
     * Loads company
     */
    fun loadCompanyOfUser(inputs: Map<CompanySearchInput, Set<UserEntity>>): CompletableFuture<Map<Pair<CompanySearchInput, UserEntity>, CompanyEntity?>>
    /**
     * Loads tasks
     */
    fun loadTasksOfUser(inputs: Map<TaskSearchInput, Set<UserEntity>>): CompletableFuture<Map<Pair<TaskSearchInput, UserEntity>, List<TaskEntity>>>
    /**
     * Loads albums
     */
    fun loadAlbumsOfUser(inputs: Map<AlbumSearchInput, Set<UserEntity>>): CompletableFuture<Map<Pair<AlbumSearchInput, UserEntity>, List<AlbumEntity>>>
    /**
     * Loads posts
     */
    fun loadPostsOfUser(inputs: Map<PostSearchInput, Set<UserEntity>>): CompletableFuture<Map<Pair<PostSearchInput, UserEntity>, List<PostEntity>>>


    /**
     * Finds addresses which matches the given conditions.
     */
    fun findAddresses(input: AddressSearchInput = AddressSearchInput()): CompletableFuture<Set<AddressEntity>>

    /**
     * Counts the number of addresses which matches the given conditions.
     */
    fun countAddresses(input: AddressSearchInput = AddressSearchInput()): CompletableFuture<Long>

    /**
     * Loads addresses having given primary keys.
     */
    fun loadAddresses(keys: Set<AddressEntity>): CompletableFuture<Set<AddressEntity>>



    /**
     * Finds companies which matches the given conditions.
     */
    fun findCompanies(input: CompanySearchInput = CompanySearchInput()): CompletableFuture<Set<CompanyEntity>>

    /**
     * Counts the number of companies which matches the given conditions.
     */
    fun countCompanies(input: CompanySearchInput = CompanySearchInput()): CompletableFuture<Long>

    /**
     * Loads companies having given primary keys.
     */
    fun loadCompanies(keys: Set<CompanyEntity>): CompletableFuture<Set<CompanyEntity>>


}