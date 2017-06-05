package ar.com.utn.proyecto.qremergencias.core.filter;

import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Component
@SuppressWarnings({"PMD.AccessorMethodGeneration", "PMD.AccessorClassGeneration"})
public class RecaptchaResponseFilter implements Filter {

    private static final String RECAPTCHA_RESPONSE_ALIAS = "recaptchaResponse";
    public static final String RECAPTCHA_RESPONSE_ORIGINAL = "g-recaptcha-response";

    private static class ModifiedHttpServerRequest extends HttpServletRequestWrapper {

        private final Map<String, String[]> parameters;

        private ModifiedHttpServerRequest(final HttpServletRequest request) {
            super(request);
            parameters = new HashMap<>(request.getParameterMap());
            parameters.put(RECAPTCHA_RESPONSE_ALIAS,
                    request.getParameterValues(RECAPTCHA_RESPONSE_ORIGINAL));
        }

        @Override
        public String getParameter(final String name) {
            final String[] params = parameters.get(name);
            if (params != null) {
                return params[0];
            }
            return null;
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            return parameters;
        }

        @Override
        public Enumeration<String> getParameterNames() {
            return Collections.enumeration(parameters.keySet());
        }

        @Override
        public String[] getParameterValues(final String name) {
            return parameters.get(name);
        }
    }

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse,
                         final FilterChain filterChain) throws IOException, ServletException {
        if (servletRequest instanceof HttpServletRequest
                && servletRequest.getParameter(RECAPTCHA_RESPONSE_ORIGINAL) != null) {
            filterChain.doFilter(new ModifiedHttpServerRequest(
                    (HttpServletRequest) servletRequest), servletResponse);
        } else {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    @Override
    public void destroy() {

    }
}
