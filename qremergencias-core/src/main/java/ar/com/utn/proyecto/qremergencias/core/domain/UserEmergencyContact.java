package ar.com.utn.proyecto.qremergencias.core.domain;


import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.io.Serializable;

@Data
public class UserEmergencyContact implements Serializable {

    @NotEmpty
    private final String firstName;
    @NotEmpty
    private final String lastName;
    @NotEmpty
    private final String phoneNumber;

    public UserEmergencyContact(final String firstName,
                                final String lastName,
                                final String phoneNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
    }
}
