package ar.com.utn.proyecto.qremergencias.core.dto.emergency;

import ar.com.utn.proyecto.qremergencias.core.domain.emergency.Hospitalization;
import ar.com.utn.proyecto.qremergencias.core.mapper.Mapper;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
public class HospitalizationDTO {

    static final Mapper<Hospitalization, HospitalizationDTO> HOSPITALIZATION_DTO_MAPPER =
            Mapper.mapping(Hospitalization.class, HospitalizationDTO.class)
                    .constructor(HospitalizationDTO::new)
                    .fields(Hospitalization::getInstitution, HospitalizationDTO::setInstitution)
                    .fields(Hospitalization::getType, HospitalizationDTO::setType, Type::valueOf)
                    .fields(Hospitalization::getDate, HospitalizationDTO::setDate)
                    .fields(Hospitalization::getReason, HospitalizationDTO::setReason);

    private String institution;
    private Type type;

    @NotNull
    private LocalDate date;

    @NotNull
    private String reason;

    public enum Type {
        cirugia, admision
    }

}
