package ar.com.utn.proyecto.qremergencias.core.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "user")
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@SuppressWarnings("PMD.ImmutableField")
public class UserFront extends User {

    private static final long serialVersionUID = -3412836946169472092L;
    private String name;
    private String lastname;
    private LocalDate birthdate;
    private String idNumber;
    private char sex;

    @OneToMany
    private List<UserEmergencyContact> contacts = new ArrayList<>();
}
