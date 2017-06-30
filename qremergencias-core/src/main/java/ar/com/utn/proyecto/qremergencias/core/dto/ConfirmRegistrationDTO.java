package ar.com.utn.proyecto.qremergencias.core.dto;

import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@SuppressWarnings("PMD.UnusedPrivateField")
public class ConfirmRegistrationDTO {

    @NotEmpty
    private String token;

    @NotEmpty
    private String lastName;

    @NotEmpty
    private String name;

    @NotNull
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;
}
