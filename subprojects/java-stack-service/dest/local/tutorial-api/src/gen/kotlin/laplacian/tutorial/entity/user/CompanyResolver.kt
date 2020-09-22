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
 * An reactive implementation of the company entity query resolver.
 */
@Component
class CompanyResolver(
    private val userRepository: UserRepository
) {

    /**
     * Loads the name of this company.
     */
    fun name(self: CompanyEntity, context: DataFetchingEnvironment): CompletableFuture<String> =
        context
        .getDataLoader<CompanyEntity, CompanyEntity>(BY_PK)
        .load(self)
        .thenApply<String>{ it.name }
    /**
     * Loads the catch_phrase of this company.
     */
    fun catchPhrase(self: CompanyEntity, context: DataFetchingEnvironment): CompletableFuture<String> =
        context
        .getDataLoader<CompanyEntity, CompanyEntity>(BY_PK)
        .load(self)
        .thenApply<String>{ it.catchPhrase }
    /**
     * Loads the bs of this company.
     */
    fun bs(self: CompanyEntity, context: DataFetchingEnvironment): CompletableFuture<String> =
        context
        .getDataLoader<CompanyEntity, CompanyEntity>(BY_PK)
        .load(self)
        .thenApply<String>{ it.bs }


    fun registerLoader(registry: DataLoaderRegistry) {
        registry.register(BY_PK, DataLoader.newMappedDataLoader<CompanyEntity, CompanyEntity> { keys ->
            userRepository
            .loadCompanies(keys)
            .thenApply{ set -> set.map{ (it to it) }.toMap() }
        })
    }

    fun registerFetcher(wiring: RuntimeWiring.Builder) = wiring.type(
        TypeRuntimeWiring.newTypeWiring("Company")
        .dataFetcher("name") { env ->
            val key = env.getSource() as CompanyEntity
            name(key, env)
        }
        .dataFetcher("catchPhrase") { env ->
            val key = env.getSource() as CompanyEntity
            catchPhrase(key, env)
        }
        .dataFetcher("bs") { env ->
            val key = env.getSource() as CompanyEntity
            bs(key, env)
        }
    )

    companion object {
        const val BY_PK = "company_byPK"
    }
}