package laplacian.tutorial.entity.photo
import laplacian.tutorial.entity.album.AlbumEntity
import laplacian.tutorial.entity.album.AlbumSearchInput

import org.dataloader.DataLoader
import org.dataloader.DataLoaderRegistry
import org.springframework.stereotype.Component
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.TypeRuntimeWiring
import graphql.schema.DataFetchingEnvironment
import java.util.concurrent.CompletableFuture

/**
 * An reactive implementation of the photo entity query resolver.
 */
@Component
class PhotoResolver(
    private val photoRepository: PhotoRepository
) {

    /**
     * Loads the album_id of this photo.
     */
    fun albumId(self: PhotoEntity, context: DataFetchingEnvironment): CompletableFuture<Int> =
        context
        .getDataLoader<PhotoEntity, PhotoEntity>(BY_PK)
        .load(self)
        .thenApply<Int>{ it.albumId }
    /**
     * Loads the title of this photo.
     */
    fun title(self: PhotoEntity, context: DataFetchingEnvironment): CompletableFuture<String> =
        context
        .getDataLoader<PhotoEntity, PhotoEntity>(BY_PK)
        .load(self)
        .thenApply<String>{ it.title }
    /**
     * Loads the url of this photo.
     */
    fun url(self: PhotoEntity, context: DataFetchingEnvironment): CompletableFuture<String> =
        context
        .getDataLoader<PhotoEntity, PhotoEntity>(BY_PK)
        .load(self)
        .thenApply<String>{ it.url }
    /**
     * Loads the thumbnailUrl of this photo.
     */
    fun thumbnailUrl(self: PhotoEntity, context: DataFetchingEnvironment): CompletableFuture<String> =
        context
        .getDataLoader<PhotoEntity, PhotoEntity>(BY_PK)
        .load(self)
        .thenApply<String>{ it.thumbnailUrl }
    /**
     * Loads the date_taken of this photo.
     */
    fun dateTaken(self: PhotoEntity, context: DataFetchingEnvironment): CompletableFuture<String> =
        context
        .getDataLoader<PhotoEntity, PhotoEntity>(BY_PK)
        .load(self)
        .thenApply<String>{ it.dateTaken }

    /**
     * Loads the album of this photo.
     */
    fun album(self: PhotoEntity, context: DataFetchingEnvironment): CompletableFuture<AlbumEntity> =
        context
        .getDataLoader<Pair<AlbumSearchInput, PhotoEntity>, AlbumEntity>(ALBUM)
        .load(AlbumSearchInput.from(context.arguments) to self)

    fun registerLoader(registry: DataLoaderRegistry) {
        registry.register(BY_PK, DataLoader.newMappedDataLoader<PhotoEntity, PhotoEntity> { keys ->
            photoRepository
            .loadPhotos(keys)
            .thenApply{ set -> set.map{ (it to it) }.toMap() }
        })
        registry.register(ALBUM, DataLoader.newMappedDataLoader<Pair<AlbumSearchInput, PhotoEntity>, AlbumEntity> { entries ->
            val input = entries.fold(mutableMapOf<AlbumSearchInput, MutableSet<PhotoEntity>>()) { acc, (albumSearchInput, photo) ->
                val photos = acc.getOrPut(albumSearchInput) { mutableSetOf<PhotoEntity>() }
                photos.add(photo)
                acc
            }
            photoRepository
            .loadAlbumOfPhoto(input)
        })
    }

    fun registerFetcher(wiring: RuntimeWiring.Builder) = wiring.type(
        TypeRuntimeWiring.newTypeWiring("Photo")
        .dataFetcher("albumId") { env ->
            val key = env.getSource() as PhotoEntity
            albumId(key, env)
        }
        .dataFetcher("title") { env ->
            val key = env.getSource() as PhotoEntity
            title(key, env)
        }
        .dataFetcher("url") { env ->
            val key = env.getSource() as PhotoEntity
            url(key, env)
        }
        .dataFetcher("thumbnailUrl") { env ->
            val key = env.getSource() as PhotoEntity
            thumbnailUrl(key, env)
        }
        .dataFetcher("dateTaken") { env ->
            val key = env.getSource() as PhotoEntity
            dateTaken(key, env)
        }
        .dataFetcher("album") { env ->
            val key = env.getSource() as PhotoEntity
            album(key, env)
        }
    )

    companion object {
        const val BY_PK = "photo_byPK"
        const val ALBUM = "photo_album"
    }
}