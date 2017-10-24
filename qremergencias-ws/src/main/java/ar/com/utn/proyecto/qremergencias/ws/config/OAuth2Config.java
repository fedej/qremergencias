package ar.com.utn.proyecto.qremergencias.ws.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.authserver.AuthorizationServerProperties;
import org.springframework.boot.autoconfigure.security.oauth2.authserver.OAuth2AuthorizationServerConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
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
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;

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
                    .antMatcher("/api/mobile/**")
                    .authorizeRequests()
                    .antMatchers("/api/mobile/**").access("#oauth2.hasScope('read')");
        }

    }

    @Configuration
    @EnableAuthorizationServer
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    protected static class OAuth2ServerConfig extends OAuth2AuthorizationServerConfiguration {

        private final RedisConnectionFactory redisConnectionFactory;
        private final AuthenticationManager authenticationManager;

        @Autowired
        public OAuth2ServerConfig(final BaseClientDetails details,
                                  final ObjectProvider<TokenStore> tokenStore,
                                  final ObjectProvider<AccessTokenConverter> tokenConverter,
                                  final AuthorizationServerProperties properties,
                                  final RedisConnectionFactory redisConnectionFactory,
                                  final AuthenticationManager authenticationManager) {
            super(details, authenticationManager, tokenStore, tokenConverter, properties);
            this.redisConnectionFactory = redisConnectionFactory;
            this.authenticationManager = authenticationManager;
        }

        @Bean
        public TokenStore tokenStore() {
            return new RedisTokenStore(redisConnectionFactory);
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
                    .tokenStore(tokenStore())
                    .approvalStoreDisabled();
        }

    }

}
