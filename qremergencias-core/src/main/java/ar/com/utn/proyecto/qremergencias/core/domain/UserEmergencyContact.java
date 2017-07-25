package ar.com.utn.proyecto.qremergencias.core.domain;


import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.io.Serializable;

@Data
public class UserEmergencyContact implements Serializable {

    @Id
    private String id;
    @NotEmpty
    private final String firstName;
    @NotEmpty
    private final String lastName;
    @NotEmpty
    private final String phoneNumber;
    @DBRef
    private final User user;

    public UserEmergencyContact(final String firstName,
                                final String lastName,
                                final String phoneNumber,
                                final User user) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.user = user;
    }
}
