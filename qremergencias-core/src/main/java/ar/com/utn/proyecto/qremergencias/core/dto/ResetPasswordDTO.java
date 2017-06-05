package ar.com.utn.proyecto.qremergencias.core.dto;

import ar.com.utn.proyecto.qremergencias.core.validation.Captcha;
import ar.com.utn.proyecto.qremergencias.core.validation.Password;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

@Data
@SuppressWarnings("PMD.UnusedPrivateField")
public class ResetPasswordDTO {

    @NotEmpty
    private String token;

    @Password
    private String newPassword;

    @Password
    private String confirmPassword;

    @Captcha
    private String recaptchaResponse;


}
