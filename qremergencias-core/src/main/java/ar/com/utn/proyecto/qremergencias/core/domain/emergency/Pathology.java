package ar.com.utn.proyecto.qremergencias.core.domain.emergency;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
public class Pathology {

    @NotNull
    private String type;
    private String description;
    @NotNull
    private LocalDate date;

}
