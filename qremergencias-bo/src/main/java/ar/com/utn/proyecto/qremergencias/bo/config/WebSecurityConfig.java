package ar.com.utn.proyecto.qremergencias.bo.config;

import ar.com.utn.proyecto.qremergencias.bo.auth.listener.AuthFailureHandler;
import ar.com.utn.proyecto.qremergencias.core.domain.User;
import ar.com.utn.proyecto.qremergencias.core.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@SuppressWarnings("PMD.SignatureDeclareThrowsException")
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    public static final String WEBJARS = "/webjars/**";
    private static final String ERROR_PAGE = "/error";

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    @Value("${qremergencias.login.loginPage}")
    private String loginPage;
    @Value("${qremergencias.login.defaultSuccessUrl}")
    private String defaultSuccessUrl;

    @Override
    protected void configure(final HttpSecurity http) throws Exception {

        http
                .formLogin()
                    .loginPage(loginPage)
                    .defaultSuccessUrl(defaultSuccessUrl, true)
                    .loginProcessingUrl(loginPage)
                    .failureHandler(new AuthFailureHandler(loginPage + "?error",
                            Collections.singletonMap(CredentialsExpiredException.class.getName(),
                                    "/forgotPassword/credentialsExpired")))
                .and()
                    .authorizeRequests()
                        .antMatchers(loginPage).anonymous()
                        .antMatchers(ERROR_PAGE).permitAll()
                        .antMatchers(WEBJARS).permitAll()
                        .antMatchers("/stylesheets/**").permitAll()
                        .antMatchers("/images/**").permitAll()
                        .antMatchers("/forgotPassword/**").anonymous()
                    .anyRequest()
                        .fullyAuthenticated();
    }

    @Autowired
    public void configureGlobal(final AuthenticationManagerBuilder auth) throws Exception {
        auth
                .userDetailsService(userDetailsService())
                .passwordEncoder(passwordEncoder);
    }

    @Override
    protected UserDetailsService userDetailsService() {

        return username -> {
            final User user = userService.findByUsername(username);

            if (user == null) {
                throw new UsernameNotFoundException(username);
            }

            return user;
        };

    }
}
