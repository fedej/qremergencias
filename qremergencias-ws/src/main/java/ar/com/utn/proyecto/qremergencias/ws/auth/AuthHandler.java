package ar.com.utn.proyecto.qremergencias.ws.auth;

import static org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.SPRING_SECURITY_FORM_USERNAME_KEY;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ar.com.utn.proyecto.qremergencias.core.domain.UserFront;
import ar.com.utn.proyecto.qremergencias.core.service.LoginAttemptCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsProcessor;

import com.fasterxml.jackson.databind.ObjectMapper;

import ar.com.utn.proyecto.qremergencias.core.dto.LoginUserDTO;

@Component
public class AuthHandler implements AuthenticationSuccessHandler, AuthenticationFailureHandler,
        LogoutSuccessHandler, AuthenticationEntryPoint {

    private static final String SHOW_CAPTCHA = "showCaptcha";

    @Autowired
    private LoginAttemptCacheService loginAttemptCacheService;

    @Autowired(required = false)
    private CorsConfiguration corsConfig;

    @Autowired(required = false)
    private CorsProcessor corsProcessor;

    @Autowired
    private ObjectMapper mapper;

    @Autowired @Lazy
    private LoginAdapter loginAdapter;

    @Autowired @Lazy
    private CsrfTokenRepository tokenRepository;

    @Value("${qremergencias.login.attempts.captcha}")
    private Integer loginAttempts;

    @Override
    public void onAuthenticationFailure(final HttpServletRequest req,
            final HttpServletResponse resp, final AuthenticationException event)
                    throws IOException, ServletException {

        process(req, resp, HttpServletResponse.SC_UNAUTHORIZED);
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

    }

    private void removeCsrfToken(final HttpServletRequest request,
                                 final HttpServletResponse response) {
        if (CsrfFilter.DEFAULT_CSRF_MATCHER.matches(request)) {
            tokenRepository.saveToken(null, request, response);
        }
    }

    private String getUsername(final HttpServletRequest req) {
        return req.getParameter(SPRING_SECURITY_FORM_USERNAME_KEY);
    }

    @Override
    public void onAuthenticationSuccess(final HttpServletRequest req,
            final HttpServletResponse resp, final Authentication auth)
                    throws IOException, ServletException {

        process(req, resp, HttpServletResponse.SC_OK);
        loginAttemptCacheService.loginSuccess(getUsername(req));
        loginAttemptCacheService.loginSuccess(req.getSession(false));
        final UserFront user = (UserFront) auth.getPrincipal();

        final LoginUserDTO dto = loginAdapter.getLoginUserDto(user, null);
        resp.setContentType(MediaType.APPLICATION_JSON_VALUE);
        resp.getWriter().print(mapper.writeValueAsString(dto));
    }

    @Override
    public void onLogoutSuccess(final HttpServletRequest req, final HttpServletResponse resp,
            final Authentication auth) throws IOException, ServletException {

        process(req, resp, HttpServletResponse.SC_OK);
    }

    private void process(final HttpServletRequest req, final HttpServletResponse resp,
            final int status) throws IOException {

        removeCsrfToken(req, resp);

        if (corsProcessor == null || corsProcessor.processRequest(corsConfig, req, resp)) {
            resp.setStatus(status);
        } else {
            resp.setStatus(500);
        }
    }

    @Override
    public void commence(final HttpServletRequest req, final HttpServletResponse res,
            final AuthenticationException event) throws IOException, ServletException {
        removeCsrfToken(req, res);
        res.sendError(HttpServletResponse.SC_UNAUTHORIZED, event.getMessage());
    }
}
