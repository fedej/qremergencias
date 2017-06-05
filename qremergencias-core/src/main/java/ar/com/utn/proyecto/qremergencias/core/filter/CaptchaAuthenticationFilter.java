package ar.com.utn.proyecto.qremergencias.core.filter;

import ar.com.utn.proyecto.qremergencias.core.service.CaptchaService;
import ar.com.utn.proyecto.qremergencias.core.service.LoginAttemptCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CaptchaAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    @Autowired
    private CaptchaService captchaService;

    @Autowired
    private LoginAttemptCacheService cacheService;

    @Value("${qremergencias.login.attempts.captcha}")
    private Integer loginAttempts;

    @Override
    public Authentication attemptAuthentication(final HttpServletRequest request,
                                                final HttpServletResponse response)
                                                throws AuthenticationException {

        if (cacheService.loginAttempts(request.getSession(false)) >= loginAttempts) {

            final String captcha = request
                                .getParameter(RecaptchaResponseFilter.RECAPTCHA_RESPONSE_ORIGINAL);

            if (StringUtils.isEmpty(captcha)) {
                throw new InsufficientAuthenticationException("Captcha response not found");
            }

            if (!captchaService.validate(request.getRemoteAddr(), captcha)) {
                throw new BadCredentialsException("Invalid captcha");
            }

        }

        return super.attemptAuthentication(request, response);
    }
}
