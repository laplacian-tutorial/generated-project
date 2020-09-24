package laplacian.tutorial.api.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.ApplicationContext
import org.springframework.util.StreamUtils
import org.springframework.web.reactive.function.server.router
import org.springframework.web.reactive.function.server.ServerRequest
import graphql.schema.GraphQLSchema
import graphql.GraphQL
import graphql.schema.idl.SchemaParser
import graphql.schema.idl.SchemaGenerator
import org.dataloader.DataLoader;
import org.dataloader.DataLoaderRegistry;
import laplacian.tutorial.api.query.QueryResolver
import laplacian.tutorial.entity.album.AlbumResolver
import laplacian.tutorial.entity.comment.CommentResolver
import laplacian.tutorial.entity.photo.PhotoResolver
import laplacian.tutorial.entity.post.PostResolver
import laplacian.tutorial.entity.task.TaskResolver
import laplacian.tutorial.entity.user.UserResolver
import laplacian.tutorial.entity.user.AddressResolver
import laplacian.tutorial.entity.user.CompanyResolver


import laplacian.tutorial.entity.indexed_comment.IndexedCommentResolver

import java.nio.charset.StandardCharsets
import graphql.schema.idl.RuntimeWiring

@Configuration
class GraphqlConfig {

    @Bean
    fun graphql(
        schema: GraphQLSchema
    ): GraphQL = GraphQL.newGraphQL(schema).build()

    @Bean
    fun buildSchema(
        context: ApplicationContext,
        runtimeWiring: RuntimeWiring
    ): GraphQLSchema {
        val parser = SchemaParser()
        val schema = context
            .getResources("classpath*:/**/*.graphql")
            .map {
                StreamUtils.copyToString(it.inputStream, StandardCharsets.UTF_8)
            }
            .joinToString("\n")
        val typeRegistry = parser.parse(schema)
        return SchemaGenerator().makeExecutableSchema(typeRegistry, runtimeWiring)
    }

    @Bean
    fun buildRuntimeWiring(
        queryResolver: QueryResolver,
        addressResolver: AddressResolver,
        albumResolver: AlbumResolver,
        commentResolver: CommentResolver,
        companyResolver: CompanyResolver,
        photoResolver: PhotoResolver,
        postResolver: PostResolver,
        taskResolver: TaskResolver,
        userResolver: UserResolver,
        indexedCommentResolver: IndexedCommentResolver,
    ): RuntimeWiring = RuntimeWiring.newRuntimeWiring().also {
        queryResolver.registerFetcher(it)
        addressResolver.registerFetcher(it)
        albumResolver.registerFetcher(it)
        commentResolver.registerFetcher(it)
        companyResolver.registerFetcher(it)
        photoResolver.registerFetcher(it)
        postResolver.registerFetcher(it)
        taskResolver.registerFetcher(it)
        userResolver.registerFetcher(it)
        indexedCommentResolver.registerFetcher(it)
    }.build()

    @Bean
    fun buildDataLoaderRegistry(
        addressResolver: AddressResolver,
        albumResolver: AlbumResolver,
        commentResolver: CommentResolver,
        companyResolver: CompanyResolver,
        photoResolver: PhotoResolver,
        postResolver: PostResolver,
        taskResolver: TaskResolver,
        userResolver: UserResolver,
    ): DataLoaderRegistry = DataLoaderRegistry().also {
        addressResolver.registerLoader(it)
        albumResolver.registerLoader(it)
        commentResolver.registerLoader(it)
        companyResolver.registerLoader(it)
        photoResolver.registerLoader(it)
        postResolver.registerLoader(it)
        taskResolver.registerLoader(it)
        userResolver.registerLoader(it)
    }
}