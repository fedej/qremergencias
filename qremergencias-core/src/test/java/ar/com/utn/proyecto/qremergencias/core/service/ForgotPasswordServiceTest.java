package ar.com.utn.proyecto.qremergencias.core.service;

import ar.com.utn.proyecto.qremergencias.core.domain.ForgotPassword;
import ar.com.utn.proyecto.qremergencias.core.domain.User;
import ar.com.utn.proyecto.qremergencias.core.repository.ForgotPasswordRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("PMD.JUnitTestContainsTooManyAsserts")
public class ForgotPasswordServiceTest {

    private static final int EXPIRATION_HOURS = 3;
    private static final String ENCODED = "ENCODED";
    private static final String NEW_PASSWORD = "newPassword";
    private static final String TOKEN = "sarasa";
    private static final String PASSWORD = "asd";

    @InjectMocks
    private ForgotPasswordService service;

    @Mock
    private ForgotPasswordRepository repository;

    @Mock
    private PasswordChangeService passwordChangeService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(service, "expirationHours", EXPIRATION_HOURS);
        when(passwordEncoder.encode(anyString())).thenReturn(ENCODED);
    }

    @Test
    public void testCreateUser() {
        when(repository.findByUserAndExpiredFalse(any(User.class))).thenReturn(null);

        final User user = new User();
        final ForgotPassword forgotPassword = service.create(user);

        verify(repository).save(any(ForgotPassword.class));

        assertNotNull(forgotPassword);
        assertNotNull(forgotPassword.getExpirationTime());
        assertNotNull(forgotPassword.getToken());
        assertNotNull(forgotPassword.getUser());
        assertEquals(user, forgotPassword.getUser());

    }

    @Test
    public void testValidateWithTokenNotFound() {
        when(repository.findByToken(anyString())).thenReturn(null);
        assertFalse(service.validate(TOKEN, PASSWORD));
    }

    @Test
    public void testValidateWithExpiredToken() {
        final ForgotPassword forgotPassword = new ForgotPassword();
        forgotPassword.setExpired(true);
        when(repository.findByToken(anyString())).thenReturn(forgotPassword);

        assertFalse(service.validate(TOKEN, PASSWORD));
    }

    @Test
    public void testValidateWithNewExpiredToken() {
        final ForgotPassword forgotPassword = new ForgotPassword();
        forgotPassword.setExpirationTime(LocalDateTime.now().minusHours(EXPIRATION_HOURS));
        when(repository.findByToken(anyString())).thenReturn(forgotPassword);

        assertFalse(service.validate(TOKEN, PASSWORD));
        assertTrue(forgotPassword.isExpired());
        verify(repository).save(forgotPassword);
    }

    @Test
    public void testValidateWithInvalidPassword() {
        final ForgotPassword forgotPassword = new ForgotPassword();
        forgotPassword.setExpirationTime(LocalDateTime.now().plusHours(1));
        forgotPassword.setUser(new User());
        when(repository.findByToken(anyString())).thenReturn(forgotPassword);

        assertFalse(service.validate(TOKEN, PASSWORD));
        verify(passwordChangeService).validate(eq(PASSWORD), any(User.class));
    }

    @Test
    public void testValidateWithPassword() {
        final ForgotPassword forgotPassword = new ForgotPassword();
        forgotPassword.setExpirationTime(LocalDateTime.now().plusHours(1));
        final User user = new User();
        forgotPassword.setUser(user);
        when(repository.findByToken(anyString())).thenReturn(forgotPassword);
        when(passwordChangeService.validate(PASSWORD, user)).thenReturn(true);

        assertTrue(service.validate(TOKEN, PASSWORD));
        verify(passwordChangeService).validate(eq(PASSWORD), eq(user));
    }

    @Test
    public void testValidateWithoutPassword() {
        final ForgotPassword forgotPassword = new ForgotPassword();
        forgotPassword.setExpirationTime(LocalDateTime.now().plusHours(1));
        final User user = new User();
        forgotPassword.setUser(user);
        when(repository.findByToken(anyString())).thenReturn(forgotPassword);

        assertTrue(service.validate(TOKEN, null));
    }

    @Test
    public void testChangePassword() {
        final ForgotPassword forgotPassword = new ForgotPassword();
        final User user = new User();
        forgotPassword.setUser(user);
        when(repository.findByToken(anyString())).thenReturn(forgotPassword);

        service.changePassword(TOKEN, NEW_PASSWORD);

        assertTrue(forgotPassword.isExpired());

        verify(passwordChangeService).changePassword(eq(user.getUsername()), eq(NEW_PASSWORD));
        verify(repository).save(forgotPassword);

    }

}
