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
    private String firstName;
    @NotEmpty
    private String lastName;
    @NotEmpty
    private String phoneNumber;
    @DBRef
    private User user;

}
