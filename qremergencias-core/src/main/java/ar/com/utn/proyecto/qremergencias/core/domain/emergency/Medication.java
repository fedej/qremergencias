package ar.com.utn.proyecto.qremergencias.core.domain.emergency;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Medication {

    private String name;
    private String description;
    private Integer amount;
    private String period;

}
