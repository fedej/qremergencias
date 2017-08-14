package ar.com.utn.proyecto.qremergencias.core.dto.emergency;

import ar.com.utn.proyecto.qremergencias.core.domain.emergency.Hospitalization;
import ar.com.utn.proyecto.qremergencias.core.mapper.Mapper;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

import static ar.com.utn.proyecto.qremergencias.core.mapper.Converters.addTimeConverter;
import static java.time.LocalTime.MIDNIGHT;

@Data
public class HospitalizationDTO {

    static final Mapper<Hospitalization, HospitalizationDTO> HOSPITALIZATION_DTO_MAPPER =
            Mapper.mapping(Hospitalization.class, HospitalizationDTO.class)
                    .constructor(HospitalizationDTO::new)
                    .fields(Hospitalization::getInstitution, HospitalizationDTO::setInstitution)
                    .fields(Hospitalization::getType, HospitalizationDTO::setType, Type::valueOf)
                    .fields(Hospitalization::getDate, HospitalizationDTO::setDate, addTimeConverter(MIDNIGHT))
                    .fields(Hospitalization::getReason, HospitalizationDTO::setReason);

    private String institution;
    private Type type;

    @NotNull
    private LocalDateTime date;

    @NotNull
    private String reason;

    public enum Type {
        SURGERY, ADMISSION
    }

}
