package ar.com.utn.proyecto.qremergencias.ws.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsProcessor;
import org.springframework.web.cors.DefaultCorsProcessor;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Configuration
@EnableWebMvc
public class WebConfig extends WebMvcConfigurerAdapter {

    @Autowired
    private Environment env;

    @Autowired
    private CsrfTokenRepository tokenRepository;

    @Override
    public void addCorsMappings(final CorsRegistry registry) {

        if (env.acceptsProfiles("gl", "local")) {
            registry.addMapping("/api/**").allowedOrigins("*").allowCredentials(true);
        }

    }

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(new CsrfInterceptor(tokenRepository));
    }

    @Slf4j
    private static class CsrfInterceptor implements HandlerInterceptor {

        private final CsrfTokenRepository tokenRepository;

        public CsrfInterceptor(final CsrfTokenRepository tokenRepository) {
            this.tokenRepository = tokenRepository;
        }

        @Override
        public boolean preHandle(final HttpServletRequest request,
                                 final HttpServletResponse response, final Object handler) {

            try {
                if (CsrfFilter.DEFAULT_CSRF_MATCHER.matches(request)) {
                    tokenRepository.saveToken(null, request, response);
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }

            return true;
        }

        @Override
        public void postHandle(final HttpServletRequest request, final HttpServletResponse response,
                               final Object handler, final ModelAndView modelAndView) {
        }

        @Override
        public void afterCompletion(final HttpServletRequest request,
                                    final HttpServletResponse response,
                                    final Object handler, final Exception ex) {
        }

    }

    @Bean
    @Profile({"gl", "local"})
    public CorsProcessor corsProcessor() {
        return new DefaultCorsProcessor();
    }

    @Bean
    @Profile({"gl", "local"})
    public CorsConfiguration corsConfig() {
        final CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowCredentials(true);
        corsConfig.addAllowedOrigin("*");
        corsConfig.addAllowedHeader("*");
        corsConfig.addAllowedMethod("GET");
        corsConfig.addAllowedMethod("POST");
        return corsConfig;
    }

}
