package ar.com.utn.proyecto.qremergencias.core.validation;

import org.springframework.beans.factory.annotation.Value;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class PasswordValidator implements ConstraintValidator<Password, String> {

    @Value("${qremergencias.password.pattern}")
    private Pattern pattern;

    @Override
    public void initialize(final Password constraintAnnotation) {

    }

    @Override
    public boolean isValid(final String value, final ConstraintValidatorContext context) {
        return value != null && (pattern == null || pattern.matcher(value).matches());
    }
}
