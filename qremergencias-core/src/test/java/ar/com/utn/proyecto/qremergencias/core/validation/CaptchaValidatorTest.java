package ar.com.utn.proyecto.qremergencias.core.validation;

import ar.com.utn.proyecto.qremergencias.core.service.CaptchaService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class CaptchaValidatorTest {

    private static final String RECAPTCHA_RESPONSE = "recaptcha-response";

    @InjectMocks
    private CaptchaValidator validator;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private CaptchaService captchaService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testValidationSuccess() {
        when(captchaService.validate(httpServletRequest.getRemoteAddr(), RECAPTCHA_RESPONSE))
                .thenReturn(true);
        assertTrue(validator.isValid(RECAPTCHA_RESPONSE, null));
    }

    @Test
    public void testValidationFails() {
        when(captchaService.validate(httpServletRequest.getRemoteAddr(), RECAPTCHA_RESPONSE))
                .thenReturn(false);
        assertFalse(validator.isValid(RECAPTCHA_RESPONSE, null));
    }

    @Test
    public void testInitialize() {
        validator.initialize(null);
    }

}
