package laplacian.tutorial.entity.album
import laplacian.tutorial.entity.user.UserEntity
import laplacian.tutorial.entity.user.UserSearchInput
import laplacian.tutorial.entity.photo.PhotoEntity
import laplacian.tutorial.entity.photo.PhotoSearchInput

import org.dataloader.DataLoader
import org.dataloader.DataLoaderRegistry
import org.springframework.stereotype.Component
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.TypeRuntimeWiring
import graphql.schema.DataFetchingEnvironment
import java.util.concurrent.CompletableFuture

/**
 * An reactive implementation of the album entity query resolver.
 */
@Component
class AlbumResolver(
    private val albumRepository: AlbumRepository
) {

    /**
     * Loads the user_id of this album.
     */
    fun userId(self: AlbumEntity, context: DataFetchingEnvironment): CompletableFuture<Int> =
        context
        .getDataLoader<AlbumEntity, AlbumEntity>(BY_PK)
        .load(self)
        .thenApply<Int>{ it.userId }
    /**
     * Loads the title of this album.
     */
    fun title(self: AlbumEntity, context: DataFetchingEnvironment): CompletableFuture<String> =
        context
        .getDataLoader<AlbumEntity, AlbumEntity>(BY_PK)
        .load(self)
        .thenApply<String>{ it.title }

    /**
     * Loads the owner of this album.
     */
    fun owner(self: AlbumEntity, context: DataFetchingEnvironment): CompletableFuture<UserEntity> =
        context
        .getDataLoader<Pair<UserSearchInput, AlbumEntity>, UserEntity>(OWNER)
        .load(UserSearchInput.from(context.arguments) to self)
    /**
     * Loads the photos of this album.
     */
    fun photos(self: AlbumEntity, context: DataFetchingEnvironment): CompletableFuture<List<PhotoEntity>> =
        context
        .getDataLoader<Pair<PhotoSearchInput, AlbumEntity>, List<PhotoEntity>>(PHOTOS)
        .load(PhotoSearchInput.from(context.arguments) to self)
        .thenApply<List<PhotoEntity>>{ it ?: emptyList() }

    fun registerLoader(registry: DataLoaderRegistry) {
        registry.register(BY_PK, DataLoader.newMappedDataLoader<AlbumEntity, AlbumEntity> { keys ->
            albumRepository
            .loadAlbums(keys)
            .thenApply{ set -> set.map{ (it to it) }.toMap() }
        })
        registry.register(OWNER, DataLoader.newMappedDataLoader<Pair<UserSearchInput, AlbumEntity>, UserEntity> { entries ->
            val input = entries.fold(mutableMapOf<UserSearchInput, MutableSet<AlbumEntity>>()) { acc, (userSearchInput, album) ->
                val albums = acc.getOrPut(userSearchInput) { mutableSetOf<AlbumEntity>() }
                albums.add(album)
                acc
            }
            albumRepository
            .loadOwnerOfAlbum(input)
        })
        registry.register(PHOTOS, DataLoader.newMappedDataLoader<Pair<PhotoSearchInput, AlbumEntity>, List<PhotoEntity>> { entries ->
            val input = entries.fold(mutableMapOf<PhotoSearchInput, MutableSet<AlbumEntity>>()) { acc, (photoSearchInput, album) ->
                val albums = acc.getOrPut(photoSearchInput) { mutableSetOf<AlbumEntity>() }
                albums.add(album)
                acc
            }
            albumRepository
            .loadPhotosOfAlbum(input)
        })
    }

    fun registerFetcher(wiring: RuntimeWiring.Builder) = wiring.type(
        TypeRuntimeWiring.newTypeWiring("Album")
        .dataFetcher("userId") { env ->
            val key = env.getSource() as AlbumEntity
            userId(key, env)
        }
        .dataFetcher("title") { env ->
            val key = env.getSource() as AlbumEntity
            title(key, env)
        }
        .dataFetcher("owner") { env ->
            val key = env.getSource() as AlbumEntity
            owner(key, env)
        }
        .dataFetcher("photos") { env ->
            val key = env.getSource() as AlbumEntity
            photos(key, env)
        }
    )

    companion object {
        const val BY_PK = "album_byPK"
        const val OWNER = "album_owner"
        const val PHOTOS = "album_photos"
    }
}