package ar.com.utn.proyecto.qremergencias.core.filter;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;

import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.verify;

public class RecaptchaResponseFilterTest {

    @Mock
    private FilterChain filterChain;

    private final RecaptchaResponseFilter filter = new RecaptchaResponseFilter();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testInit() throws ServletException {
        filter.init(null);
    }

    @Test
    public void testDoFilterWithCaptcha() throws IOException, ServletException {

        final MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.setParameter(RecaptchaResponseFilter.RECAPTCHA_RESPONSE_ORIGINAL,
                "recaptchaValue");

        filter.doFilter(servletRequest, new MockHttpServletResponse(), filterChain);

        verify(filterChain).doFilter(isA(HttpServletRequestWrapper.class),
                isA(MockHttpServletResponse.class));
    }

    @Test
    public void testDoFilterWithoutCaptcha() throws IOException, ServletException {

        filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), filterChain);

        verify(filterChain).doFilter(isA(MockHttpServletRequest.class),
                isA(MockHttpServletResponse.class));
    }

    @Test
    public void testDestroy() {
        filter.destroy();
    }
}