package cz.csas.datastructures.patrol.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * Given to you, nothing to implement here.
 *
 * `gradlew bootRun` serves the delivered frontend from this very server, so the
 * browser talks to the same origin and CORS never enters the picture. It only
 * matters when the frontend is served on its own port by `gradlew frontend`:
 * a page on http://localhost:5500 calling an API on http://localhost:8080 is a
 * cross origin request, and the browser throws the response away unless the
 * server answers with an Access-Control-Allow-Origin header naming that origin.
 *
 * The allowed origins come from `patrol.cors.allowed-origins` in application.yaml.
 * Add yours there if you serve the frontend from somewhere else.
 */
@Configuration
class WebCorsConfiguration(
    @param:Value("\${patrol.cors.allowed-origins}") private val allowedOrigins: Array<String>,
) : WebMvcConfigurer {

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/api/**")
            .allowedOrigins(*allowedOrigins)
            .allowedMethods("GET", "POST", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .maxAge(3600)
    }
}
