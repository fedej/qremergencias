package ar.com.utn.proyecto.qremergencias.core.dto;

import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

@Data
@SuppressWarnings("PMD.UnusedPrivateField")
public class ConfirmRegistrationDTO {

    @NotEmpty
    private String token;
    @NotEmpty
    private String lastName;
    @NotEmpty
    private String name;
    @NotEmpty
    private String birthDate;
}
