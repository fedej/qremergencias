package ar.com.utn.proyecto.qremergencias.ws.auth;

import ar.com.utn.proyecto.qremergencias.core.domain.UserFront;
import ar.com.utn.proyecto.qremergencias.core.dto.LoginUserDTO;
import ar.com.utn.proyecto.qremergencias.core.service.LoginAttemptCacheService;
import ar.com.utn.proyecto.qremergencias.ws.controller.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

import static org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.SPRING_SECURITY_FORM_USERNAME_KEY;

@Component
public class AuthHandler implements AuthenticationSuccessHandler, AuthenticationFailureHandler,
        LogoutSuccessHandler, AuthenticationEntryPoint {

    private static final String SHOW_CAPTCHA = "showCaptcha";

    @Autowired
    private LoginAttemptCacheService loginAttemptCacheService;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private GlobalExceptionHandler globalExceptionHandler;

    @Value("${qremergencias.login.attempts.captcha}")
    private Integer loginAttempts;

    @Override
    public void onAuthenticationFailure(final HttpServletRequest req,
            final HttpServletResponse resp, final AuthenticationException event)
                    throws IOException, ServletException {

        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        final HttpSession session = req.getSession(false);
        loginAttemptCacheService.failedLogin(session);
        loginAttemptCacheService.failedLogin(getUsername(req));

        if (loginAttemptCacheService.loginAttempts(session) >= loginAttempts) {
            session.setAttribute(SHOW_CAPTCHA, true);
            final Cookie cookie = new Cookie(SHOW_CAPTCHA, Boolean.TRUE.toString());
            cookie.setMaxAge(1800);
            cookie.setHttpOnly(false);
            cookie.setPath("/");

            if (req.isSecure()) {
                cookie.setSecure(true);
            }

            resp.addCookie(cookie);
        }

        resp.setContentType(MediaType.APPLICATION_JSON_VALUE);
        final ResponseEntity<GlobalExceptionHandler.ApiError> error = globalExceptionHandler.error(event);
        resp.getWriter().print(mapper.writeValueAsString(error.getBody()));

    }

    private String getUsername(final HttpServletRequest req) {
        return req.getParameter(SPRING_SECURITY_FORM_USERNAME_KEY);
    }

    @Override
    public void onAuthenticationSuccess(final HttpServletRequest req,
            final HttpServletResponse resp, final Authentication auth)
                    throws IOException, ServletException {

        resp.setStatus(HttpServletResponse.SC_OK);
        loginAttemptCacheService.loginSuccess(getUsername(req));
        loginAttemptCacheService.loginSuccess(req.getSession(false));
        final UserFront user = (UserFront) auth.getPrincipal();

        final LoginUserDTO dto =
                new LoginUserDTO(user.getName(), user.getLastname(), user.getRoles(), user.getEmail());

        resp.setContentType(MediaType.APPLICATION_JSON_VALUE);
        resp.getWriter().print(mapper.writeValueAsString(dto));
    }

    @Override
    public void commence(final HttpServletRequest req, final HttpServletResponse res,
            final AuthenticationException event) throws IOException, ServletException {
        res.sendError(HttpServletResponse.SC_UNAUTHORIZED, event.getMessage());
    }

    @Override
    public void onLogoutSuccess(final HttpServletRequest request,
                                final HttpServletResponse response,
                                final Authentication authentication)
            throws IOException, ServletException {

    }
}
