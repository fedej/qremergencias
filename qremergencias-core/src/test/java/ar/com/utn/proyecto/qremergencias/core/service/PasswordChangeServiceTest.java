package ar.com.utn.proyecto.qremergencias.core.service;

import ar.com.utn.proyecto.qremergencias.core.domain.PasswordChange;
import ar.com.utn.proyecto.qremergencias.core.domain.User;
import ar.com.utn.proyecto.qremergencias.core.repository.PasswordChangeRepository;
import ar.com.utn.proyecto.qremergencias.core.repository.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PasswordChangeServiceTest {

    @InjectMocks
    private PasswordChangeService service = new PasswordChangeService();

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordChangeRepository passwordChangeRepository;

    @Mock
    private User user;

    @Mock
    PasswordChange changePassword;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testValidatePasswordChangeStringUser() {
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
        when(passwordEncoder.matches(null, null)).thenReturn(true);
        when(passwordEncoder.encode(anyString())).thenReturn("encodeddPassword!#%");
        buildMocks();

        String newPassword = "LaClaveEsNuevita";

        boolean changePasswordValid = service.validate(changePassword.getPassword(), newPassword,
                user);
        assertTrue(changePasswordValid);

    }

    @Test
    public void testValidatePasswordChangeStringUserNoMatch() {
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
        String newPassword = "LaClaveEsNuevita";
        boolean changePasswordValid = service.validate(changePassword.getPassword(), newPassword,
                user);
        assertFalse(changePasswordValid);

    }

    @Test
    public void testValidateStringUser() {
        buildMocks();
        String newPassword = "LaClaveEsNuevita";
        boolean isValid = service.validate(newPassword, user);
        assertTrue(isValid);
    }

    @Test
    public void testValidateStringUserNullPassword() {
        buildMocks();
        String newPassword = null;
        boolean isValid = service.validate(newPassword, user);
        assertFalse(isValid);
    }

    @Test
    public void testChangePassword() {
        final String newPassword = "LaClaveEsNuevita";
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(passwordChangeRepository.save(any(PasswordChange.class))).thenReturn(null);
        when(passwordEncoder.encode(anyString())).thenReturn("claveEncodeada");
        service.changePassword(user, newPassword);
        verify(userRepository).save(user);
        verify(passwordChangeRepository).save(any(PasswordChange.class));
    }

    private void buildMocks() {
        Page<PasswordChange> changesPage = mock(Page.class);
        List<PasswordChange> content = new ArrayList<>();
        PasswordChange pc;
        for (int i = 0; i < 10; i++) {
            pc = new PasswordChange();
            pc.setId(i * 10L);
            pc.setPassword("laClaveAnteriorDistinta" + i);
            content.add(pc);
        }

        when(changesPage.getContent()).thenReturn(content);
        when(passwordChangeRepository.findByUser(any(User.class), any(PageRequest.class)))
                .thenReturn(changesPage);
    }
}
