package laplacian.tutorial.api.configuration

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.elasticsearch.client.reactive.ReactiveElasticsearchClient
import org.springframework.data.elasticsearch.client.reactive.ReactiveRestClients
import org.springframework.data.elasticsearch.client.ClientConfiguration
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchTemplate
import org.springframework.data.elasticsearch.core.convert.ElasticsearchConverter
import org.springframework.data.elasticsearch.core.convert.MappingElasticsearchConverter
import org.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext
import org.springframework.web.reactive.function.client.ExchangeStrategies

@Configuration
class ElasticSearchConfig(
    @Value("\${search_engine_client.default.endpoints}")
    private val endpoints: String,
) {

    @Bean
    fun elasticSearchClient(): ReactiveElasticsearchClient = ReactiveRestClients.create(
        ClientConfiguration
        .builder()
        .connectedTo(endpoints)
        .withWebClientConfigurer { client ->
            client.mutate().exchangeStrategies(
                ExchangeStrategies
                .builder()
                .codecs{ it.defaultCodecs().maxInMemorySize(-1) }
                .build()
            )
            .build()
        }
        .build()
    )

    @Bean
    fun elasticsearchConverter(): ElasticsearchConverter =
        MappingElasticsearchConverter(SimpleElasticsearchMappingContext())

    @Bean
    fun reactiveElasticsearchOperations(
        client: ReactiveElasticsearchClient,
        converter: ElasticsearchConverter,
    ): ReactiveElasticsearchOperations = ReactiveElasticsearchTemplate(client, converter)
}
