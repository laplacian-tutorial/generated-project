package laplacian.tutorial.entity.photo
import java.util.concurrent.CompletableFuture
import laplacian.tutorial.entity.album.AlbumEntity
import laplacian.tutorial.entity.album.AlbumSearchInput


typealias Records = List<Record>
typealias Record = Map<String, Any?>

/**
 * The interface which abstracts the details of the data access concerning photos.
 */
interface PhotoRepository {


    /**
     * Finds photos which matches the given conditions.
     */
    fun findPhotos(input: PhotoSearchInput = PhotoSearchInput()): CompletableFuture<Set<PhotoEntity>>

    /**
     * Counts the number of photos which matches the given conditions.
     */
    fun countPhotos(input: PhotoSearchInput = PhotoSearchInput()): CompletableFuture<Long>

    /**
     * Loads photos having given primary keys.
     */
    fun loadPhotos(keys: Set<PhotoEntity>): CompletableFuture<Set<PhotoEntity>>

    /**
     * Loads album
     */
    fun loadAlbumOfPhoto(inputs: Map<AlbumSearchInput, Set<PhotoEntity>>): CompletableFuture<Map<Pair<AlbumSearchInput, PhotoEntity>, AlbumEntity>>

}