package ar.com.utn.proyecto.qremergencias.bo.auth.listener;

import ar.com.utn.proyecto.qremergencias.core.config.ApiLoginConfigurer;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.ExceptionMappingAuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public class AuthFailureHandler extends ExceptionMappingAuthenticationFailureHandler {

    public AuthFailureHandler(final String defaultFailureUrl,
                              final Map<String, String> exceptionMappings) {
        super();
        setDefaultFailureUrl(defaultFailureUrl);
        setExceptionMappings(exceptionMappings);
    }

    @Override
    public void onAuthenticationFailure(final HttpServletRequest request,
                                        final HttpServletResponse response,
                                        final AuthenticationException exception)
            throws IOException, ServletException {

        request.getSession().setAttribute(ApiLoginConfigurer.USERNAME_PARAMETER,
                request.getParameter(ApiLoginConfigurer.USERNAME_PARAMETER));
        super.onAuthenticationFailure(request, response, exception);
    }



}
