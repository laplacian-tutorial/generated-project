package laplacian.tutorial.api

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.ComponentScan

/**
 * The launcher class for the tutorial_api service.
 */
@SpringBootApplication
@ComponentScan(basePackages = [
    "laplacian.tutorial"
])
class TutorialApiApplication

/**
 * The entry point of this service.
 */
fun main(args: Array<String>) {
    SpringApplication.run(TutorialApiApplication::class.java, *args)
}