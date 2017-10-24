package ar.com.utn.proyecto.qremergencias.ws.config;

import ar.com.utn.proyecto.qremergencias.core.dto.LoginUserDTO;
import ar.com.utn.proyecto.qremergencias.ws.controller.GlobalExceptionHandler.ApiError;
import com.fasterxml.classmate.TypeResolver;
import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.builders.OperationBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.ResponseMessageBuilder;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiDescription;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ResponseMessage;
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

import static ar.com.utn.proyecto.qremergencias.ws.controller.GlobalExceptionHandler.BAD_INPUT;
import static ar.com.utn.proyecto.qremergencias.ws.controller.GlobalExceptionHandler.BAD_INPUT_CODE;
import static ar.com.utn.proyecto.qremergencias.ws.controller.GlobalExceptionHandler.UNEXPECTED_ERROR;
import static ar.com.utn.proyecto.qremergencias.ws.controller.GlobalExceptionHandler.UNEXPECTED_ERROR_CODE;
import static ar.com.utn.proyecto.qremergencias.ws.controller.GlobalExceptionHandler.UNAUTHORIZED_ERROR;
import static ar.com.utn.proyecto.qremergencias.ws.controller.GlobalExceptionHandler.UNAUTHORIZED_ERROR_CODE;
import static com.google.common.base.Predicates.and;
import static com.google.common.base.Predicates.not;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static springfox.documentation.builders.PathSelectors.regex;
import static springfox.documentation.schema.AlternateTypeRules.newRule;
import static springfox.documentation.service.ApiInfo.DEFAULT_CONTACT;

@EnableSwagger2
@Configuration
@Profile("!prod")
@ComponentScan(basePackages = "ar.com.utn.proyecto.qremergencias.ws.controller")
@SuppressWarnings("PMD.TooManyStaticImports")
public class SwaggerConfig {

    private final TypeResolver typeResolver;
    private final Environment environment;

    @Autowired
    public SwaggerConfig(final TypeResolver typeResolver, final Environment environment) {
        this.typeResolver = typeResolver;
        this.environment = environment;
    }

    @Bean
    // To access the generated swagger
    // http://localhost:8082/qremergencias/v2/api-docs
    // Then it must be copied to src/main/resources/swagger.json
    @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
    public Docket swaggerSpringMvcPlugin() {

        final List<ResponseMessage> responseMessageList = new ArrayList<>();
        responseMessageList
                .add(new ResponseMessageBuilder()
                        .code(BAD_INPUT_CODE)
                        .message(BAD_INPUT)
                        .responseModel(new ModelRef(ApiError.class.getSimpleName()))
                        .build());
        responseMessageList
                .add(new ResponseMessageBuilder()
                        .code(UNEXPECTED_ERROR_CODE)
                        .message(UNEXPECTED_ERROR)
                        .responseModel(new ModelRef(ApiError.class.getSimpleName()))
                        .build());

        Predicate<String> androidRegex = regex("/mobile/.*");
        if (!environment.acceptsProfiles("android")) {
            androidRegex = not(androidRegex);
        }

        return new Docket(DocumentationType.SWAGGER_2)
                .protocols(Collections.singleton("http"))
                .ignoredParameterTypes(AuthenticationPrincipal.class, Resource.class,
                        Pageable.class)
                .alternateTypeRules(
                        newRule(typeResolver.resolve(Resource.class),
                                typeResolver.resolve(MultipartFile.class)))
                .forCodeGeneration(true)
                .apiInfo(new ApiInfo("QR Emergencias WS", "API Rest QR Emergencias",
                        "1.0.0", "", DEFAULT_CONTACT, "", "", Collections.emptyList()))
                .globalResponseMessage(RequestMethod.POST, responseMessageList)
                .globalResponseMessage(RequestMethod.PATCH, responseMessageList)
                .globalResponseMessage(RequestMethod.GET, responseMessageList)
                .globalResponseMessage(RequestMethod.DELETE, responseMessageList)
                .globalResponseMessage(RequestMethod.PUT, responseMessageList)
                .additionalModels(typeResolver.resolve(ApiError.class),
                        typeResolver.resolve(LoginUserDTO.class))
                .select()
                .paths(and(not(regex("/error.*")), not(regex("/oauth.*")), regex("/.*"), androidRegex))
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
                            .produces(Collections.singleton(APPLICATION_JSON_UTF8_VALUE))
                            .responseMessages(Sets.newHashSet(
                                    new ResponseMessageBuilder()
                                            .code(HttpStatus.OK.value())
                                            .message(HttpStatus.OK.name())
                                            .responseModel(new ModelRef("LoginUserDTO"))
                                            .build(),
                                    new ResponseMessageBuilder()
                                            .code(UNAUTHORIZED_ERROR_CODE)
                                            .message(UNAUTHORIZED_ERROR)
                                            .responseModel(new ModelRef(ApiError.class.getSimpleName()))
                                            .build()
                            ))
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
