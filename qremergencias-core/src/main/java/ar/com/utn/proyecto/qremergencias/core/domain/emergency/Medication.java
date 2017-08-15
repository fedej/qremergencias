package ar.com.utn.proyecto.qremergencias.core.domain.emergency;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@javax.persistence.Entity
public class Medication {

    @javax.persistence.Id
    private Long id;

    private String name;
    private String description;
    private Integer amount;
    private String period;

}
