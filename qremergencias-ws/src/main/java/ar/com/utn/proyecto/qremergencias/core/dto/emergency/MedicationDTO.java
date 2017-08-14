package ar.com.utn.proyecto.qremergencias.core.dto.emergency;

import ar.com.utn.proyecto.qremergencias.core.domain.emergency.Medication;
import ar.com.utn.proyecto.qremergencias.core.domain.emergency.Pathology;
import ar.com.utn.proyecto.qremergencias.core.mapper.Mapper;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MedicationDTO {

    static final Mapper<Medication, MedicationDTO> MEDICATION_DTO_MAPPER =
            Mapper.mapping(Medication.class, MedicationDTO.class)
                    .constructor(MedicationDTO::new)
                    .fields(Medication::getName, MedicationDTO::setName)
                    .fields(Medication::getDescription, MedicationDTO::setDescription)
                    .fields(Medication::getAmount, MedicationDTO::setAmount)
                    .fields(Medication::getPeriod, MedicationDTO::setPeriod, MedicationDTO.Period::valueOf);

    private String name;
    private String description;
    private Integer amount;
    private Period period;

    public enum Period {
        DAILY, WEEKLY, MONTHLY
    }

}
