package ar.com.utn.proyecto.qremergencias.core.validation;

import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AgeValidatorTest {

    private static final AgeValidator AGE_VALIDATOR = new AgeValidator(13);

    @Test
    public void testValidationSuccess() {
        assertTrue(AGE_VALIDATOR.validate(19, 9, 1990));
    }

    @Test
    public void testValidationFailure() {
        final LocalDate now = LocalDate.now();
        assertFalse(
                AGE_VALIDATOR.validate(now.getDayOfMonth(), now.getMonthValue(), now.getYear()));
    }

    @Test
    public void testValidationTodayBirtday() {
        LocalDate now = LocalDate.now();
        now = now.minusYears(13);
        assertTrue(
                AGE_VALIDATOR.validate(now.getDayOfMonth(), now.getMonthValue(), now.getYear()));
    }

    @Test
    public void testValidationTomorrowBirtday() {
        final LocalDate birtday = LocalDate.now().minusYears(13).plusDays(1);
        assertFalse(AGE_VALIDATOR.validate(birtday.getDayOfMonth(), birtday.getMonthValue(),
                birtday.getYear()));
    }
    
    @Test
    public void testValidationTenDaysForBirtday() {
        final LocalDate birtday = LocalDate.now().minusYears(13).minusDays(10);
        assertTrue(AGE_VALIDATOR.validate(birtday.getDayOfMonth(), birtday.getMonthValue(),
                birtday.getYear()));
    }
}
