package ar.com.utn.proyecto.qremergencias.core.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotEmpty;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class UserEmergencyContact implements Serializable {

    @NotEmpty
    private String firstName;
    @NotEmpty
    private String lastName;
    @NotEmpty
    private String phoneNumber;

    private boolean primary;

    public UserEmergencyContact(final String firstName,
                                final String lastName,
                                final String phoneNumber,
                                final boolean primary) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.primary = primary;
    }
}
