package ar.com.utn.proyecto.qremergencias.bo.auth.listener;

import ar.com.utn.proyecto.qremergencias.core.service.LoginAttemptCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpSession;

@Component
public class AuthenticationFailureListener implements
        ApplicationListener<AuthenticationFailureBadCredentialsEvent> {

    @Autowired
    private LoginAttemptCacheService loginAttemptCacheService;

    @Value("${qremergencias.login.attempts.captcha}")
    private Integer loginAttempts;

    @Override
    public void onApplicationEvent(final AuthenticationFailureBadCredentialsEvent event) {
        final String name = event.getAuthentication().getName();
        loginAttemptCacheService.failedLogin(name);

        final HttpSession session = getSession();
        loginAttemptCacheService.failedLogin(session);

        if (loginAttemptCacheService.loginAttempts(session) >= loginAttempts) {
            session.setAttribute("showCaptcha", true);
        }

    }

    private HttpSession getSession() {
        final ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes.getRequest().getSession(false);
    }

}
