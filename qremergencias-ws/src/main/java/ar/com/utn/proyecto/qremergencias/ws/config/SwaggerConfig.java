package ar.com.utn.proyecto.qremergencias.ws.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Pageable;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import static com.google.common.base.Predicates.and;
import static com.google.common.base.Predicates.not;
import static springfox.documentation.builders.PathSelectors.regex;

@EnableSwagger2
@Configuration
//@Profile("local")
@ComponentScan(basePackages = "ar.com.utn.proyecto.qremergencias.ws.controller")
public class SwaggerConfig {

    @Bean
    // To access the generated swagger
    // http://localhost:8080/v2/api-docs
    // Then it must be copied to src/main/resources/swagger.json
    public Docket swaggerSpringMvcPlugin() {
        return new Docket(DocumentationType.SWAGGER_2)
            .forCodeGeneration(true)
            .ignoredParameterTypes(Pageable.class)
            .select()
            .paths(and(not(regex("/error.*")),regex("/.*")))
            .build();
    }

}
