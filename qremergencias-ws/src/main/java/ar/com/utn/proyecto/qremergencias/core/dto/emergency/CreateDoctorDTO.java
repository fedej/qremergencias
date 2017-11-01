package ar.com.utn.proyecto.qremergencias.core.dto.emergency;

import ar.com.utn.proyecto.qremergencias.core.dto.CreateUserDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.NotEmpty;

@Data
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("PMD.UnusedPrivateField")
public class CreateDoctorDTO extends CreateUserDTO {
    @NotEmpty
    private String registrationNumber;

}
