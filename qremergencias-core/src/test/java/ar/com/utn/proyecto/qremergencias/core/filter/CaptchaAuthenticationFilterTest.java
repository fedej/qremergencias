package ar.com.utn.proyecto.qremergencias.core.filter;

import ar.com.utn.proyecto.qremergencias.core.service.CaptchaService;
import ar.com.utn.proyecto.qremergencias.core.service.LoginAttemptCacheService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.test.util.ReflectionTestUtils;

import javax.servlet.http.HttpSession;
import java.net.InetAddress;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("PMD.JUnitTestContainsTooManyAsserts")
public class CaptchaAuthenticationFilterTest {

    private static final int MAX_ATTEMPTS = 5;

    @InjectMocks
    private final CaptchaAuthenticationFilter filter = new CaptchaAuthenticationFilter();

    @Mock
    private CaptchaService captchaService;

    @Mock
    private LoginAttemptCacheService cacheService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(filter, "loginAttempts", MAX_ATTEMPTS);
        filter.setAuthenticationManager(createAuthenticationManager());
    }

    @Test
    public void testAttemptAuthentication() {
        when(cacheService.loginAttempts(any(HttpSession.class))).thenReturn(MAX_ATTEMPTS - 1);

        final MockHttpServletRequest request = new MockHttpServletRequest("POST", "/");
        request.addParameter(UsernamePasswordAuthenticationFilter.SPRING_SECURITY_FORM_USERNAME_KEY,
                "rod");
        request.addParameter(UsernamePasswordAuthenticationFilter.SPRING_SECURITY_FORM_PASSWORD_KEY,
                "koala");

        final Authentication result = filter.attemptAuthentication(request,
                new MockHttpServletResponse());

        assertNotNull(result);
        assertEquals(InetAddress.getLoopbackAddress().getHostAddress(),
                ((WebAuthenticationDetails) result.getDetails()).getRemoteAddress());
    }

    @Test(expected = InsufficientAuthenticationException.class)
    public void testAttemptAuthenticationWithoutRequiredCaptcha() {
        when(cacheService.loginAttempts(any(HttpSession.class))).thenReturn(MAX_ATTEMPTS);
        filter.attemptAuthentication(new MockHttpServletRequest(), new MockHttpServletResponse());
    }

    @Test(expected = BadCredentialsException.class)
    public void testAttemptAuthenticationWithInvalidCaptcha() {
        when(cacheService.loginAttempts(any(HttpSession.class))).thenReturn(MAX_ATTEMPTS);
        when(captchaService.validate(anyString(), anyString())).thenReturn(false);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter(RecaptchaResponseFilter.RECAPTCHA_RESPONSE_ORIGINAL, "captcha");
        filter.attemptAuthentication(request, new MockHttpServletResponse());
    }

    private AuthenticationManager createAuthenticationManager() {
        final AuthenticationManager am = mock(AuthenticationManager.class);
        when(am.authenticate(any(Authentication.class)))
                .thenAnswer(invocation -> invocation.getArguments()[0]);
        return am;
    }
}