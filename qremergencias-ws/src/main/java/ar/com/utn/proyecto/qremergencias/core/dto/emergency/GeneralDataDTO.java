package ar.com.utn.proyecto.qremergencias.core.dto.emergency;

import ar.com.utn.proyecto.qremergencias.core.domain.emergency.GeneralData;
import ar.com.utn.proyecto.qremergencias.core.mapper.Mapper;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.util.List;

@Data
@NoArgsConstructor
public class GeneralDataDTO {

    static final Mapper<GeneralData, GeneralDataDTO> GENERAL_DATA_DTO_MAPPER =
            Mapper.mapping(GeneralData.class, GeneralDataDTO.class)
                    .constructor(GeneralDataDTO::new)
                    .fields(GeneralData::getBloodType, GeneralDataDTO::setBloodType)
                    .fields(GeneralData::isOrganDonor, GeneralDataDTO::setOrganDonor)
                    .fields(GeneralData::getAllergies, GeneralDataDTO::setAllergies)
                    .fields(d -> d.getAllergies() != null && !d.getAllergies().isEmpty(),
                            GeneralDataDTO::setAllergic);

    @Length(min = 1, max = 3)
    private String bloodType;

    private boolean organDonor;

    private boolean allergic;
    private List<String> allergies;

}
