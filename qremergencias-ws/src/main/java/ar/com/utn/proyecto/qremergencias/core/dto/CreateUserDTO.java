package ar.com.utn.proyecto.qremergencias.core.dto;

import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

import ar.com.utn.proyecto.qremergencias.core.validation.Password;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("PMD.UnusedPrivateField")
public class CreateUserDTO extends RegisterUserDTO {

    @Password
    private String password;

    @NotEmpty
    @Size(max = 50, min = 4)
    private String name;

    @NotEmpty
    @Size(max = 50, min = 4)
    private String lastname;

    @Email
    @NotEmpty
    private String email;

    @NotEmpty
    private String role;

}
