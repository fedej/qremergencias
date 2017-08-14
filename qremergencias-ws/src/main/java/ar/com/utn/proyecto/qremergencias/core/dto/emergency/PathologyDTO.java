package ar.com.utn.proyecto.qremergencias.core.dto.emergency;

import ar.com.utn.proyecto.qremergencias.core.domain.emergency.Pathology;
import ar.com.utn.proyecto.qremergencias.core.mapper.Mapper;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

import static ar.com.utn.proyecto.qremergencias.core.mapper.Converters.addTimeConverter;
import static java.time.LocalTime.MIDNIGHT;

@Data
public class PathologyDTO {


    static final Mapper<Pathology, PathologyDTO> PATHOLOGY_DTO_MAPPER =
            Mapper.mapping(Pathology.class, PathologyDTO.class)
                    .constructor(PathologyDTO::new)
                    .fields(Pathology::getDescription, PathologyDTO::setDescription)
                    .fields(Pathology::getType, PathologyDTO::setType, PathologyDTO.Type::valueOf)
                    .fields(Pathology::getDate, PathologyDTO::setDate, addTimeConverter(MIDNIGHT));

    private Type type;

    public enum Type {
        ANATOMICAL, CLINICAL, MOLECULAR, ORAL
    }

    @NotNull
    private String description;

    @NotNull
    private LocalDateTime date;



}
