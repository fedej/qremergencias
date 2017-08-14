package ar.com.utn.proyecto.qremergencias.core.domain.emergency;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.util.List;

@Data
@NoArgsConstructor
public class GeneralData {

    @Length(min = 1, max = 3)
    private String bloodType;

    private boolean organDonor;
    private List<String> allergies;

}
