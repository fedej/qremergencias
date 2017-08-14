package ar.com.utn.proyecto.qremergencias.core.domain.emergency;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
public class Hospitalization {

    private String institution;
    private String type;

    @NotNull
    private LocalDate date;

    @NotNull
    private String reason;

}
