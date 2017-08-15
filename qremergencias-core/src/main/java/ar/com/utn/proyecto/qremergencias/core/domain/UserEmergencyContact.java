package ar.com.utn.proyecto.qremergencias.core.domain;


import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;

@Data
@Entity
public class UserEmergencyContact implements Serializable {

    @Id
    private Long id;

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
