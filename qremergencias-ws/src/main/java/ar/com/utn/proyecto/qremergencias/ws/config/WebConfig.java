package ar.com.utn.proyecto.qremergencias.ws.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
@EnableWebMvc
public class WebConfig {

    @Component
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public static class CorsFilter implements Filter {

        @Override
        public void destroy() { }

        @Override
        public void doFilter(final ServletRequest req, final ServletResponse res,
                             final FilterChain chain)
                throws IOException, ServletException {
            final HttpServletResponse response = (HttpServletResponse) res;
            final HttpServletRequest request = (HttpServletRequest) req;

            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Methods",
                    "POST, PUT, GET, OPTIONS, DELETE");
            response.setHeader("Access-Control-Max-Age", "3600");
            response.setHeader("Access-Control-Allow-Headers", "*");
            response.setHeader("Access-Control-Allow-Credentials", "true");

            if (!RequestMethod.OPTIONS.name().equalsIgnoreCase(request.getMethod())) {
                chain.doFilter(req, res);
            }
        }

        @Override
        public void init(final FilterConfig config) throws ServletException {

        }
    }

}
