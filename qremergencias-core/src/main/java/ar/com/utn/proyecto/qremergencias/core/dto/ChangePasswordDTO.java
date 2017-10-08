package ar.com.utn.proyecto.qremergencias.core.dto;

import ar.com.utn.proyecto.qremergencias.core.validation.Captcha;
import ar.com.utn.proyecto.qremergencias.core.validation.Password;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

@Data
@SuppressWarnings("PMD.UnusedPrivateField")
public class ChangePasswordDTO {


    private String id;

    @NotEmpty
    private String password;

    @Password
    private String newPassword;

    @Password
    private String confirmPassword;


    private String recaptchaResponse;

}
