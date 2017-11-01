package ar.com.utn.proyecto.qremergencias.core.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;

@Data
@Entity
@NoArgsConstructor
public class UserEmergencyContact implements Serializable {

    @Id
    private Long id;

    @NotEmpty
    private String firstName;
    @NotEmpty
    private String lastName;
    @NotEmpty
    private String phoneNumber;

    private boolean primaryContact;

    public UserEmergencyContact(final String firstName,
                                final String lastName,
                                final String phoneNumber,
                                final boolean primary) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.primaryContact = primary;
    }
}
