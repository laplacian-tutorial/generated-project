package laplacian.tutorial.entity.user
import laplacian.tutorial.entity.user.UserEntity
import laplacian.tutorial.entity.user.UserSearchInput

import org.dataloader.DataLoader
import org.dataloader.DataLoaderRegistry
import org.springframework.stereotype.Component
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.TypeRuntimeWiring
import graphql.schema.DataFetchingEnvironment
import java.util.concurrent.CompletableFuture

/**
 * An reactive implementation of the address entity query resolver.
 */
@Component
class AddressResolver(
    private val userRepository: UserRepository
) {

    /**
     * Loads the street of this address.
     */
    fun street(self: AddressEntity, context: DataFetchingEnvironment): CompletableFuture<String> =
        context
        .getDataLoader<AddressEntity, AddressEntity>(BY_PK)
        .load(self)
        .thenApply<String>{ it.street }
    /**
     * Loads the suite of this address.
     */
    fun suite(self: AddressEntity, context: DataFetchingEnvironment): CompletableFuture<String> =
        context
        .getDataLoader<AddressEntity, AddressEntity>(BY_PK)
        .load(self)
        .thenApply<String>{ it.suite }
    /**
     * Loads the city of this address.
     */
    fun city(self: AddressEntity, context: DataFetchingEnvironment): CompletableFuture<String> =
        context
        .getDataLoader<AddressEntity, AddressEntity>(BY_PK)
        .load(self)
        .thenApply<String>{ it.city }
    /**
     * Loads the zipcode of this address.
     */
    fun zipcode(self: AddressEntity, context: DataFetchingEnvironment): CompletableFuture<String> =
        context
        .getDataLoader<AddressEntity, AddressEntity>(BY_PK)
        .load(self)
        .thenApply<String>{ it.zipcode }
    /**
     * Loads the latitude of this address.
     */
    fun latitude(self: AddressEntity, context: DataFetchingEnvironment): CompletableFuture<String> =
        context
        .getDataLoader<AddressEntity, AddressEntity>(BY_PK)
        .load(self)
        .thenApply<String>{ it.latitude }
    /**
     * Loads the longitude of this address.
     */
    fun longitude(self: AddressEntity, context: DataFetchingEnvironment): CompletableFuture<String> =
        context
        .getDataLoader<AddressEntity, AddressEntity>(BY_PK)
        .load(self)
        .thenApply<String>{ it.longitude }


    fun registerLoader(registry: DataLoaderRegistry) {
        registry.register(BY_PK, DataLoader.newMappedDataLoader<AddressEntity, AddressEntity> { keys ->
            userRepository
            .loadAddresses(keys)
            .thenApply{ set -> set.map{ (it to it) }.toMap() }
        })
    }

    fun registerFetcher(wiring: RuntimeWiring.Builder) = wiring.type(
        TypeRuntimeWiring.newTypeWiring("Address")
        .dataFetcher("street") { env ->
            val key = env.getSource() as AddressEntity
            street(key, env)
        }
        .dataFetcher("suite") { env ->
            val key = env.getSource() as AddressEntity
            suite(key, env)
        }
        .dataFetcher("city") { env ->
            val key = env.getSource() as AddressEntity
            city(key, env)
        }
        .dataFetcher("zipcode") { env ->
            val key = env.getSource() as AddressEntity
            zipcode(key, env)
        }
        .dataFetcher("latitude") { env ->
            val key = env.getSource() as AddressEntity
            latitude(key, env)
        }
        .dataFetcher("longitude") { env ->
            val key = env.getSource() as AddressEntity
            longitude(key, env)
        }
    )

    companion object {
        const val BY_PK = "address_byPK"
    }
}