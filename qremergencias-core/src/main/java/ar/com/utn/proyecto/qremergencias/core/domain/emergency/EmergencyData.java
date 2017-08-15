package ar.com.utn.proyecto.qremergencias.core.domain.emergency;

import ar.com.utn.proyecto.qremergencias.core.domain.UserFront;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.DBRef;

import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import java.time.LocalDate;
import java.util.List;

@Data
@javax.persistence.Entity
@SuppressWarnings("PMD.TooManyFields")
public class EmergencyData {

    @Id
    @javax.persistence.Id
    private String id;

    @Version
    private Long version;

    @OneToOne
    private GeneralData general;

    @javax.persistence.ElementCollection
    private List<Hospitalization> surgeries;

    @javax.persistence.ElementCollection
    private List<Hospitalization> hospitalizations;

    @javax.persistence.ElementCollection
    private List<Medication> medications;

    @javax.persistence.ElementCollection
    private List<Pathology> pathologies;
    private LocalDate lastMedicalCheck;

    @DBRef
    @ManyToOne
    private UserFront user;

}
