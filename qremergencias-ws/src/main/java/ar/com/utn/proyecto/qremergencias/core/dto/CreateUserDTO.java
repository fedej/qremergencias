package ar.com.utn.proyecto.qremergencias.core.dto;

import ar.com.utn.proyecto.qremergencias.core.validation.Captcha;
import ar.com.utn.proyecto.qremergencias.core.validation.Password;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Pattern;

@Data
@EqualsAndHashCode
@SuppressWarnings("PMD.UnusedPrivateField")
public class CreateUserDTO {

    @Password
    private String password;

    @Email
    @NotEmpty
    private String email;

    @NotEmpty
    @Pattern(regexp = "ROLE_PACIENTE|ROLE_MEDICO")
    private String role;

    //@Captcha
    //private String recaptchaResponse;

}
