package ar.com.utn.proyecto.qremergencias.core.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("PMD.JUnitTestContainsTooManyAsserts")
public class LoginAttemptCacheServiceTest {

    private static final String USER = "user";

    @InjectMocks
    private LoginAttemptCacheService service;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private UserService userService;

    private final Cache sessionCache = new ConcurrentMapCache("userCache");
    private final Cache userCache = new ConcurrentMapCache("sessionCache");

    private static final int ALLOWED_ATTEMPTS_PER_USER = 3;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(service, "allowedAttemptsPerUser", ALLOWED_ATTEMPTS_PER_USER);
        ReflectionTestUtils.setField(service, "userCacheName", "userCache");
        ReflectionTestUtils.setField(service, "sessionCacheName", "sessionCache");

        when(cacheManager.getCache("userCache")).thenReturn(userCache);
        when(cacheManager.getCache("sessionCache")).thenReturn(sessionCache);
        service.init();
    }

    @Test
    public void testLoginSuccessSession() {
        final MockHttpSession session = new MockHttpSession();
        service.loginSuccess(session);
        assertNull(sessionCache.get(session, Integer.class));
    }

    @Test
    public void testLoginSuccessUsername() {
        service.loginSuccess(USER);
        assertNull(userCache.get(USER, Integer.class));
    }

    @Test
    public void testLoginAttemptsSession() {
        final MockHttpSession session = new MockHttpSession();
        service.failedLogin(session);
        service.failedLogin(session);

        assertEquals(Integer.valueOf(2), service.loginAttempts(session));
    }

    @Test
    public void testLoginFailedSession() {
        final MockHttpSession session = new MockHttpSession();
        assertNull(sessionCache.get(session, Integer.class));
        service.failedLogin(session);
        assertEquals(Integer.valueOf(1), service.loginAttempts(session));
    }

    @Test
    public void testMultpileLoginAttemptsBlockUser() {
        when(userService.blockUser(anyString())).thenReturn(true);

        assertNull(userCache.get(USER, Integer.class));

        for (int i = 0; i < ALLOWED_ATTEMPTS_PER_USER; i++) {
            service.failedLogin(USER);
        }

        assertNull(userCache.get(USER, Integer.class));
        verify(userService).blockUser(USER);
    }

}
