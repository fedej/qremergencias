package ar.com.utn.proyecto.qremergencias.core.validation;

import ar.com.utn.proyecto.qremergencias.core.service.CaptchaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class CaptchaValidator implements ConstraintValidator<Captcha, String> {

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private CaptchaService captchaService;

    @Override
    public void initialize(final Captcha constraintAnnotation) {

    }

    @Override
    public boolean isValid(final String response, final ConstraintValidatorContext context) {
        return captchaService.validate(httpServletRequest.getRemoteAddr(), response);
    }
}