package ar.com.utn.proyecto.qremergencias.bo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.resource.WebJarsResourceResolver;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.TemplateResolver;

@Configuration
public class MvcConfig extends WebMvcConfigurerAdapter {

    private static final int CACHE_PERIOD = 604800; // 1 WEEK IN SECONDS

    @Autowired
    private MessageSource messageSource;

    @Value("${qremergencias.login.loginPage}")
    private String loginPage;

    @Override
    public void addViewControllers(final ViewControllerRegistry registry) {
        registry.addViewController("/dashboard").setViewName("dashboard");
        registry.addViewController(loginPage).setViewName("login");
    }

    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {

        if (!registry.hasMappingForPattern(WebSecurityConfig.WEBJARS)) {
            registry.addResourceHandler(WebSecurityConfig.WEBJARS)
                    .addResourceLocations("classpath:/META-INF/resources/webjars/")
                    .setCachePeriod(CACHE_PERIOD)
                    .resourceChain(true)
                    .addResolver(new WebJarsResourceResolver());
        }

        if (!registry.hasMappingForPattern("/**")) {
            registry.addResourceHandler("/**")
                    .addResourceLocations("classpath:/static/")
                    .setCachePeriod(CACHE_PERIOD);
        }

    }

    @Bean
    public TemplateResolver emailTemplateResolver() {
        final ClassLoaderTemplateResolver emailTemplateResolver = new ClassLoaderTemplateResolver();
        emailTemplateResolver.setPrefix("templates/mail/");
        emailTemplateResolver.setTemplateMode("HTML5");
        emailTemplateResolver.setSuffix(".html");
        emailTemplateResolver.setOrder(1);

        return emailTemplateResolver;
    }

    @Override
    public Validator getValidator() {
        final LocalValidatorFactoryBean factory = new LocalValidatorFactoryBean();
        factory.setValidationMessageSource(messageSource);
        return factory;
    }
}
