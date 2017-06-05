package ar.com.utn.proyecto.qremergencias.core.validation;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.regex.Pattern;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PasswordValidatorTest {

    private static final PasswordValidator PASSWORD_VALIDATOR = new PasswordValidator();

    @Before
    public void setUp() {
        ReflectionTestUtils.setField(PASSWORD_VALIDATOR, "pattern",
                Pattern.compile("((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&/()?¿¡$%]).{8,64})"));
    }

    @Test
    public void testValidationSuccess() {
        assertTrue(PASSWORD_VALIDATOR.isValid("Passw0rd!", null));
    }

    @Test
    public void testValidationFails() {
        assertFalse(PASSWORD_VALIDATOR.isValid("UNSECURE", null));
    }

    @Test
    public void testValidationFailsOnNullValue() {
        assertFalse(PASSWORD_VALIDATOR.isValid(null, null));
    }

    @Test
    public void testInitialize() {
        PASSWORD_VALIDATOR.initialize(null);
    }

}
