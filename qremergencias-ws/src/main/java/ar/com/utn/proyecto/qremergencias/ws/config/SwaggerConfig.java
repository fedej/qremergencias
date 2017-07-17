package ar.com.utn.proyecto.qremergencias.ws.config;

import com.fasterxml.classmate.TypeResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import springfox.documentation.builders.OperationBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiDescription;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.ApiListingScannerPlugin;
import springfox.documentation.spi.service.contexts.DocumentationContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.spring.web.readers.operation.CachingOperationNameGenerator;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Predicates.and;
import static com.google.common.base.Predicates.not;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static springfox.documentation.builders.PathSelectors.regex;
import static springfox.documentation.service.ApiInfo.DEFAULT_CONTACT;

@EnableSwagger2
@Configuration
//@Profile("local")
@ComponentScan(basePackages = "ar.com.utn.proyecto.qremergencias.ws.controller")
public class SwaggerConfig {

    @Bean
    // To access the generated swagger
    // http://localhost:8082/qremergencias/v2/api-docs
    // Then it must be copied to src/main/resources/swagger.json
    public Docket swaggerSpringMvcPlugin() {
        return new Docket(DocumentationType.SWAGGER_2)
                .protocols(Collections.singleton("http"))
                .ignoredParameterTypes(AuthenticationPrincipal.class)
                .forCodeGeneration(true)
                .apiInfo(new ApiInfo("QR Emergencias WS", "API Rest QR Emergencias",
                        "1.0.0", "", DEFAULT_CONTACT,"", "", Collections.emptyList()))
                .ignoredParameterTypes(Pageable.class)
                .select()
                .paths(and(not(regex("/error.*")), regex("/.*")))
                .build();
    }

    @Bean
    public ApiListingScannerPlugin loginSwaggerDocumentation() {
        return new LoginSwaggerDocumentation();
    }

    @Bean
    public ApiListingScannerPlugin logoutSwaggerDocumentation() {
        return new LogoutSwaggerDocumentation();
    }

    private static class LoginSwaggerDocumentation implements ApiListingScannerPlugin {

        @Override
        public List<ApiDescription> apply(final DocumentationContext context) {
            return new ArrayList<>(Collections.singletonList(new ApiDescription(
                    "/api/login", "Login Endpoint", Collections.singletonList(
                    new OperationBuilder(new CachingOperationNameGenerator())
                            .codegenMethodNameStem("loginUsingPOST")
                            .consumes(Collections.singleton(APPLICATION_FORM_URLENCODED_VALUE))
                            .method(HttpMethod.POST)
                            .tags(Collections.singleton("user-front-controller"))
                            .parameters(Arrays.asList(new ParameterBuilder()
                                            .type(new TypeResolver().resolve(String.class))
                                            .name("username")
                                            .parameterType("form")
                                            .parameterAccess("access")
                                            .required(true)
                                            .modelRef(new ModelRef("string")) //<5>
                                            .build(),
                                    new ParameterBuilder()
                                            .type(new TypeResolver().resolve(String.class))
                                            .name("password")
                                            .parameterType("form")
                                            .parameterAccess("access")
                                            .required(true)
                                            .modelRef(new ModelRef("string")) //<5>
                                            .build(),
                                    new ParameterBuilder()
                                            .type(new TypeResolver().resolve(String.class))
                                            .name("g-recaptcha-response")
                                            .parameterType("form")
                                            .parameterAccess("access")
                                            .required(false)
                                            .modelRef(new ModelRef("string")) //<5>
                                            .build()))
                            .build()),
                    false)));
        }

        @Override
        public boolean supports(final DocumentationType delimiter) {
            return DocumentationType.SWAGGER_2.equals(delimiter);
        }

    }

    private static class LogoutSwaggerDocumentation implements ApiListingScannerPlugin {

        @Override
        public List<ApiDescription> apply(final DocumentationContext context) {
            return new ArrayList<>(Collections.singletonList(new ApiDescription(
                    "/api/logout", "Logout Endpoint", Collections.singletonList(
                    new OperationBuilder(new CachingOperationNameGenerator())
                        .codegenMethodNameStem("logoutUsingPOST")
                        .consumes(Collections.singleton(APPLICATION_FORM_URLENCODED_VALUE))
                        .method(HttpMethod.POST)
                        .tags(Collections.singleton("user-front-controller"))
                        .build()), false)));
        }

        @Override
        public boolean supports(final DocumentationType delimiter) {
            return DocumentationType.SWAGGER_2.equals(delimiter);
        }

    }

}
