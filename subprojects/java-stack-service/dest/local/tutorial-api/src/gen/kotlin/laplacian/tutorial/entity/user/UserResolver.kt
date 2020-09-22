package laplacian.tutorial.entity.user
import laplacian.tutorial.entity.task.TaskEntity
import laplacian.tutorial.entity.task.TaskSearchInput
import laplacian.tutorial.entity.album.AlbumEntity
import laplacian.tutorial.entity.album.AlbumSearchInput
import laplacian.tutorial.entity.post.PostEntity
import laplacian.tutorial.entity.post.PostSearchInput

import org.dataloader.DataLoader
import org.dataloader.DataLoaderRegistry
import org.springframework.stereotype.Component
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.TypeRuntimeWiring
import graphql.schema.DataFetchingEnvironment
import java.util.concurrent.CompletableFuture

/**
 * An reactive implementation of the user entity query resolver.
 */
@Component
class UserResolver(
    private val userRepository: UserRepository
) {

    /**
     * Loads the name of this user.
     */
    fun name(self: UserEntity, context: DataFetchingEnvironment): CompletableFuture<String> =
        context
        .getDataLoader<UserEntity, UserEntity>(BY_PK)
        .load(self)
        .thenApply<String>{ it.name }
    /**
     * Loads the username of this user.
     */
    fun username(self: UserEntity, context: DataFetchingEnvironment): CompletableFuture<String> =
        context
        .getDataLoader<UserEntity, UserEntity>(BY_PK)
        .load(self)
        .thenApply<String>{ it.username }
    /**
     * Loads the email of this user.
     */
    fun email(self: UserEntity, context: DataFetchingEnvironment): CompletableFuture<String> =
        context
        .getDataLoader<UserEntity, UserEntity>(BY_PK)
        .load(self)
        .thenApply<String>{ it.email }
    /**
     * Loads the phone of this user.
     */
    fun phone(self: UserEntity, context: DataFetchingEnvironment): CompletableFuture<String> =
        context
        .getDataLoader<UserEntity, UserEntity>(BY_PK)
        .load(self)
        .thenApply<String>{ it.phone }
    /**
     * Loads the website of this user.
     */
    fun website(self: UserEntity, context: DataFetchingEnvironment): CompletableFuture<String> =
        context
        .getDataLoader<UserEntity, UserEntity>(BY_PK)
        .load(self)
        .thenApply<String>{ it.website }

    /**
     * Loads the address of this user.
     */
    fun address(self: UserEntity, context: DataFetchingEnvironment): CompletableFuture<AddressEntity> =
        context
        .getDataLoader<Pair<AddressSearchInput, UserEntity>, AddressEntity>(ADDRESS)
        .load(AddressSearchInput.from(context.arguments) to self)
    /**
     * Loads the company of this user.
     */
    fun company(self: UserEntity, context: DataFetchingEnvironment): CompletableFuture<CompanyEntity> =
        context
        .getDataLoader<Pair<CompanySearchInput, UserEntity>, CompanyEntity>(COMPANY)
        .load(CompanySearchInput.from(context.arguments) to self)
    /**
     * Loads the tasks of this user.
     */
    fun tasks(self: UserEntity, context: DataFetchingEnvironment): CompletableFuture<List<TaskEntity>> =
        context
        .getDataLoader<Pair<TaskSearchInput, UserEntity>, List<TaskEntity>>(TASKS)
        .load(TaskSearchInput.from(context.arguments) to self)
        .thenApply<List<TaskEntity>>{ it ?: emptyList() }
    /**
     * Loads the albums of this user.
     */
    fun albums(self: UserEntity, context: DataFetchingEnvironment): CompletableFuture<List<AlbumEntity>> =
        context
        .getDataLoader<Pair<AlbumSearchInput, UserEntity>, List<AlbumEntity>>(ALBUMS)
        .load(AlbumSearchInput.from(context.arguments) to self)
        .thenApply<List<AlbumEntity>>{ it ?: emptyList() }
    /**
     * Loads the posts of this user.
     */
    fun posts(self: UserEntity, context: DataFetchingEnvironment): CompletableFuture<List<PostEntity>> =
        context
        .getDataLoader<Pair<PostSearchInput, UserEntity>, List<PostEntity>>(POSTS)
        .load(PostSearchInput.from(context.arguments) to self)
        .thenApply<List<PostEntity>>{ it ?: emptyList() }

    fun registerLoader(registry: DataLoaderRegistry) {
        registry.register(BY_PK, DataLoader.newMappedDataLoader<UserEntity, UserEntity> { keys ->
            userRepository
            .loadUsers(keys)
            .thenApply{ set -> set.map{ (it to it) }.toMap() }
        })
        registry.register(ADDRESS, DataLoader.newMappedDataLoader<Pair<AddressSearchInput, UserEntity>, AddressEntity> { entries ->
            val input = entries.fold(mutableMapOf<AddressSearchInput, MutableSet<UserEntity>>()) { acc, (addressSearchInput, user) ->
                val users = acc.getOrPut(addressSearchInput) { mutableSetOf<UserEntity>() }
                users.add(user)
                acc
            }
            userRepository
            .loadAddressOfUser(input)
        })
        registry.register(COMPANY, DataLoader.newMappedDataLoader<Pair<CompanySearchInput, UserEntity>, CompanyEntity> { entries ->
            val input = entries.fold(mutableMapOf<CompanySearchInput, MutableSet<UserEntity>>()) { acc, (companySearchInput, user) ->
                val users = acc.getOrPut(companySearchInput) { mutableSetOf<UserEntity>() }
                users.add(user)
                acc
            }
            userRepository
            .loadCompanyOfUser(input)
        })
        registry.register(TASKS, DataLoader.newMappedDataLoader<Pair<TaskSearchInput, UserEntity>, List<TaskEntity>> { entries ->
            val input = entries.fold(mutableMapOf<TaskSearchInput, MutableSet<UserEntity>>()) { acc, (taskSearchInput, user) ->
                val users = acc.getOrPut(taskSearchInput) { mutableSetOf<UserEntity>() }
                users.add(user)
                acc
            }
            userRepository
            .loadTasksOfUser(input)
        })
        registry.register(ALBUMS, DataLoader.newMappedDataLoader<Pair<AlbumSearchInput, UserEntity>, List<AlbumEntity>> { entries ->
            val input = entries.fold(mutableMapOf<AlbumSearchInput, MutableSet<UserEntity>>()) { acc, (albumSearchInput, user) ->
                val users = acc.getOrPut(albumSearchInput) { mutableSetOf<UserEntity>() }
                users.add(user)
                acc
            }
            userRepository
            .loadAlbumsOfUser(input)
        })
        registry.register(POSTS, DataLoader.newMappedDataLoader<Pair<PostSearchInput, UserEntity>, List<PostEntity>> { entries ->
            val input = entries.fold(mutableMapOf<PostSearchInput, MutableSet<UserEntity>>()) { acc, (postSearchInput, user) ->
                val users = acc.getOrPut(postSearchInput) { mutableSetOf<UserEntity>() }
                users.add(user)
                acc
            }
            userRepository
            .loadPostsOfUser(input)
        })
    }

    fun registerFetcher(wiring: RuntimeWiring.Builder) = wiring.type(
        TypeRuntimeWiring.newTypeWiring("User")
        .dataFetcher("name") { env ->
            val key = env.getSource() as UserEntity
            name(key, env)
        }
        .dataFetcher("username") { env ->
            val key = env.getSource() as UserEntity
            username(key, env)
        }
        .dataFetcher("email") { env ->
            val key = env.getSource() as UserEntity
            email(key, env)
        }
        .dataFetcher("phone") { env ->
            val key = env.getSource() as UserEntity
            phone(key, env)
        }
        .dataFetcher("website") { env ->
            val key = env.getSource() as UserEntity
            website(key, env)
        }
        .dataFetcher("address") { env ->
            val key = env.getSource() as UserEntity
            address(key, env)
        }
        .dataFetcher("company") { env ->
            val key = env.getSource() as UserEntity
            company(key, env)
        }
        .dataFetcher("tasks") { env ->
            val key = env.getSource() as UserEntity
            tasks(key, env)
        }
        .dataFetcher("albums") { env ->
            val key = env.getSource() as UserEntity
            albums(key, env)
        }
        .dataFetcher("posts") { env ->
            val key = env.getSource() as UserEntity
            posts(key, env)
        }
    )

    companion object {
        const val BY_PK = "user_byPK"
        const val ADDRESS = "user_address"
        const val COMPANY = "user_company"
        const val TASKS = "user_tasks"
        const val ALBUMS = "user_albums"
        const val POSTS = "user_posts"
    }
}