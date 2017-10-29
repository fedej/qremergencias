package ar.com.utn.proyecto.qremergencias.ws.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.authserver.AuthorizationServerProperties;
import org.springframework.boot.autoconfigure.security.oauth2.authserver.OAuth2AuthorizationServerConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.security.oauth2.provider.token.AccessTokenConverter;
import org.springframework.security.oauth2.provider.token.TokenStore;

@Configuration
public class OAuth2Config {

    @Configuration
    @EnableResourceServer
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    protected static class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {

        @Override
        public void configure(final ResourceServerSecurityConfigurer resources) throws Exception {
            resources.resourceId("qremergencias");
        }

        @Override
        public void configure(final HttpSecurity http) throws Exception {
            http
                    .antMatcher("/api/mobile/emergencyData")
                    .authorizeRequests()
                    .anyRequest().permitAll()
            .and()
                    .antMatcher("/api/mobile/**")
                    .authorizeRequests()
                    .antMatchers("/api/mobile/**").access("#oauth2.hasScope('read')");
        }

    }

    @Configuration
    @EnableAuthorizationServer
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    protected static class OAuth2ServerConfig extends OAuth2AuthorizationServerConfiguration {

        private final TokenStore tokenStore;
        private final AuthenticationManager authenticationManager;

        @Autowired
        public OAuth2ServerConfig(final BaseClientDetails details,
                                  final ObjectProvider<TokenStore> tokenStoreObjectProvider,
                                  final ObjectProvider<AccessTokenConverter> tokenConverter,
                                  final AuthorizationServerProperties properties,
                                  final TokenStore tokenStore,
                                  final AuthenticationManager authenticationManager) {
            super(details, authenticationManager, tokenStoreObjectProvider, tokenConverter, properties);
            this.tokenStore = tokenStore;
            this.authenticationManager = authenticationManager;
        }

        @Override
        public void configure(final AuthorizationServerSecurityConfigurer security) throws Exception {
            super.configure(security);
            security.allowFormAuthenticationForClients();
        }

        @Override
        public void configure(final AuthorizationServerEndpointsConfigurer endpoints) {
            endpoints
                    .authenticationManager(authenticationManager)
                    .tokenStore(tokenStore)
                    .approvalStoreDisabled();
        }

    }

}
