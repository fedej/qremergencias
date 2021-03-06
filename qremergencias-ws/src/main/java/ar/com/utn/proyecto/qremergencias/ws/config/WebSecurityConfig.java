package ar.com.utn.proyecto.qremergencias.ws.config;

import ar.com.utn.proyecto.qremergencias.core.domain.UserFront;
import ar.com.utn.proyecto.qremergencias.ws.auth.AuthHandler;
import ar.com.utn.proyecto.qremergencias.ws.service.UserFrontService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
@SuppressWarnings("PMD.SignatureDeclareThrowsException")
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthHandler authHandler;

    @Autowired
    private UserFrontService userFrontService;

    @Autowired
    private CorsConfiguration corsConfiguration;

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        http.csrf().disable();
        http.cors().configurationSource((request) -> corsConfiguration);
        http
                .antMatcher("/api/**")
                .formLogin()
                    .loginProcessingUrl("/api/login")
                    .successHandler(authHandler)
                    .failureHandler(authHandler)
                .and()
                    .authorizeRequests()
                        .anyRequest()
                            .permitAll()
                .and()
                    .logout()
                        .logoutUrl("/api/logout")
                            .logoutSuccessHandler(authHandler)
                        .permitAll()
                .and()
                    .exceptionHandling()
                        .authenticationEntryPoint(authHandler)
                .and()
                    .sessionManagement()
                        .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
                        .maximumSessions(3);
    }

    @Autowired
    public void configureGlobal(final AuthenticationManagerBuilder auth) throws Exception {
        auth
                .userDetailsService(userDetailsService())
                .passwordEncoder(passwordEncoder);
    }

    @Bean
    public TokenStore tokenStore(final RedisConnectionFactory redisConnectionFactory) {
        return new RedisTokenStore(redisConnectionFactory);
    }

    protected UserDetailsService userDetailsService() {

        return username -> {
            final UserFront userFront = userFrontService.findByUsername(username);

            if (userFront == null) {
                throw new UsernameNotFoundException(username);
            }

            return userFront;
        };

    }

}
