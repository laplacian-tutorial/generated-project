package laplacian.tutorial.entity.album
import java.util.concurrent.CompletableFuture
import laplacian.tutorial.entity.user.UserEntity
import laplacian.tutorial.entity.user.UserSearchInput
import laplacian.tutorial.entity.photo.PhotoEntity
import laplacian.tutorial.entity.photo.PhotoSearchInput


typealias Records = List<Record>
typealias Record = Map<String, Any?>

/**
 * The interface which abstracts the details of the data access concerning albums.
 */
interface AlbumRepository {


    /**
     * Finds albums which matches the given conditions.
     */
    fun findAlbums(input: AlbumSearchInput = AlbumSearchInput()): CompletableFuture<Set<AlbumEntity>>

    /**
     * Counts the number of albums which matches the given conditions.
     */
    fun countAlbums(input: AlbumSearchInput = AlbumSearchInput()): CompletableFuture<Long>

    /**
     * Loads albums having given primary keys.
     */
    fun loadAlbums(keys: Set<AlbumEntity>): CompletableFuture<Set<AlbumEntity>>

    /**
     * Loads owner
     */
    fun loadOwnerOfAlbum(inputs: Map<UserSearchInput, Set<AlbumEntity>>): CompletableFuture<Map<Pair<UserSearchInput, AlbumEntity>, UserEntity>>
    /**
     * Loads photos
     */
    fun loadPhotosOfAlbum(inputs: Map<PhotoSearchInput, Set<AlbumEntity>>): CompletableFuture<Map<Pair<PhotoSearchInput, AlbumEntity>, List<PhotoEntity>>>

}