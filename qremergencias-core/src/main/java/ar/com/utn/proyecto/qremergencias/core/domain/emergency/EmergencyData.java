package ar.com.utn.proyecto.qremergencias.core.domain.emergency;

import ar.com.utn.proyecto.qremergencias.core.domain.UserFront;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.time.LocalDate;
import java.util.List;

@Data
@SuppressWarnings("PMD.TooManyFields")
public class EmergencyData {

    @Id
    private String id;

    @Version
    private Long version;

    private GeneralData general;
    private List<Hospitalization> surgeries;
    private List<Hospitalization> hospitalizations;
    private List<Medication> medications;
    private List<Pathology> pathologies;
    private LocalDate lastMedicalCheck;

    private String uuid;

    @DBRef
    private UserFront user;

}
