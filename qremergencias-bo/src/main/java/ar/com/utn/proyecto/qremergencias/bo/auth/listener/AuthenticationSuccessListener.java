package ar.com.utn.proyecto.qremergencias.bo.auth.listener;

import ar.com.utn.proyecto.qremergencias.core.service.LoginAttemptCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpSession;

@Component
public class AuthenticationSuccessListener implements
        ApplicationListener<AuthenticationSuccessEvent> {

    @Autowired
    private LoginAttemptCacheService loginAttemptCacheService;

    @Override
    public void onApplicationEvent(final AuthenticationSuccessEvent event) {
        final String name = event.getAuthentication().getName();
        loginAttemptCacheService.loginSuccess(name);
        loginAttemptCacheService.loginSuccess(getSession());
    }

    private HttpSession getSession() {
        final ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes.getRequest().getSession(false);
    }

}
