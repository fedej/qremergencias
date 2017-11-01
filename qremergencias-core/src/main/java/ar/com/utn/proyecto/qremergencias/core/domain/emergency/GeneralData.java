package ar.com.utn.proyecto.qremergencias.core.domain.emergency;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@javax.persistence.Entity
public class GeneralData {

    @javax.persistence.Id
    private Long id;

    @Length(min = 1, max = 3)
    private String bloodType;

    private boolean organDonor;

    @javax.persistence.ElementCollection
    private List<String> allergies = new ArrayList<String>();

    private LocalDate lastMedicalCheck;

}
