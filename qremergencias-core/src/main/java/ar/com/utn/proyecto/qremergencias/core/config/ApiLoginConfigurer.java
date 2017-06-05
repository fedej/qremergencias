package ar.com.utn.proyecto.qremergencias.core.config;

import ar.com.utn.proyecto.qremergencias.core.filter.CaptchaAuthenticationFilter;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.configurers.AbstractAuthenticationFilterConfigurer;
import org.springframework.security.web.authentication.ui.DefaultLoginPageGeneratingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

public final class ApiLoginConfigurer<H extends HttpSecurityBuilder<H>> extends
        AbstractAuthenticationFilterConfigurer<H, ApiLoginConfigurer<H>,
                CaptchaAuthenticationFilter> {

    public static final String USERNAME_PARAMETER = "username";

    public ApiLoginConfigurer() {
        super(new CaptchaAuthenticationFilter(), null);
        getAuthenticationFilter().setUsernameParameter(USERNAME_PARAMETER);
        getAuthenticationFilter().setPasswordParameter("password");
    }

    @SuppressWarnings("PMD.UselessOverridingMethod")
    public ApiLoginConfigurer<H> loginPage(final String loginPage) {
        return super.loginPage(loginPage);
    }

    @Override
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public void init(final H http) throws Exception {
        super.init(http);
        initDefaultLoginFilter(http);
    }

    @Override
    protected RequestMatcher createLoginProcessingUrlMatcher(final String loginProcessingUrl) {
        return new AntPathRequestMatcher(loginProcessingUrl, "POST");
    }

    private String getUsernameParameter() {
        return getAuthenticationFilter().getUsernameParameter();
    }

    private String getPasswordParameter() {
        return getAuthenticationFilter().getPasswordParameter();
    }

    private void initDefaultLoginFilter(final H http) {
        final DefaultLoginPageGeneratingFilter loginPageGeneratingFilter = http
                .getSharedObject(DefaultLoginPageGeneratingFilter.class);
        if (loginPageGeneratingFilter != null && !isCustomLoginPage()) {
            loginPageGeneratingFilter.setFormLoginEnabled(true);
            loginPageGeneratingFilter.setUsernameParameter(getUsernameParameter());
            loginPageGeneratingFilter.setPasswordParameter(getPasswordParameter());
            loginPageGeneratingFilter.setLoginPageUrl(getLoginPage());
            loginPageGeneratingFilter.setFailureUrl(getFailureUrl());
            loginPageGeneratingFilter.setAuthenticationUrl(getLoginProcessingUrl());
        }
    }
}
