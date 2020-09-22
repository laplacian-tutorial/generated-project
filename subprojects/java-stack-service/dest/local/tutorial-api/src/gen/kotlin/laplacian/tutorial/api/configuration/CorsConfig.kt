package laplacian.tutorial.api.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
import org.springframework.web.cors.reactive.CorsWebFilter

@Configuration
class CorsConfig {
    @Bean
    fun corsWebFilter(): CorsWebFilter {
        val corsConfig = CorsConfiguration().apply {
            applyPermitDefaultValues()
        }
        val source = UrlBasedCorsConfigurationSource().apply {
             registerCorsConfiguration("/**", corsConfig);
        }
        return  CorsWebFilter(source);
    }
}