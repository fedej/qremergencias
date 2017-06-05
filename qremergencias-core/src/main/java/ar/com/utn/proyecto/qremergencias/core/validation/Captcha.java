package ar.com.utn.proyecto.qremergencias.core.validation;

import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(FIELD)
@Retention(RUNTIME)
@Constraint(validatedBy = {CaptchaValidator.class})
@Documented
@NotEmpty
public @interface Captcha {

    String message() default "{validation.invalid.captcha}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}