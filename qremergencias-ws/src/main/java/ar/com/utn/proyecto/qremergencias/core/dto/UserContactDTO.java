package ar.com.utn.proyecto.qremergencias.core.dto;

import ar.com.utn.proyecto.qremergencias.core.domain.UserFront;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

@Data
public class UserContactDTO {

    private String id;
    private String firstName;
    private String lastName;
    private String phoneNumber;

    public UserContactDTO() {

    }

    public UserContactDTO(String id, String firstName, String lastName, String phoneNumber) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
    }
}
