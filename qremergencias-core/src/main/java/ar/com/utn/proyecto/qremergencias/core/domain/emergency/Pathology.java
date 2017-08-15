package ar.com.utn.proyecto.qremergencias.core.domain.emergency;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@javax.persistence.Entity
public class Pathology {

    @javax.persistence.Id
    private Long id;

    @NotNull
    private String type;
    @NotNull
    private String description;
    @NotNull
    private LocalDate date;

}
