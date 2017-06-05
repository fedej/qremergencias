package ar.com.utn.proyecto.qremergencias.bo.dto;

import ar.com.utn.proyecto.qremergencias.core.validation.Captcha;
import lombok.Data;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

@Data
@SuppressWarnings("PMD.UnusedPrivateField")
public class ForgotPasswordDTO {

    @NotEmpty
    private String username;

    @Email
    @NotEmpty
    private String email;

    @Captcha
    private String recaptchaResponse;

}
