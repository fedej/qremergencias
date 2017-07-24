package ar.com.utn.proyecto.qremergencias.core.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "user")
public class UserFront extends User {

    private static final long serialVersionUID = -3412836946169472092L;
    private String name;
    private String lastname;
    private LocalDate birthdate;
    private String numeroDocumento;
}
