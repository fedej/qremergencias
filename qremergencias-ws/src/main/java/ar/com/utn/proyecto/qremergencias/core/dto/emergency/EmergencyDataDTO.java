package ar.com.utn.proyecto.qremergencias.core.dto.emergency;

import ar.com.utn.proyecto.qremergencias.core.domain.emergency.EmergencyData;
import ar.com.utn.proyecto.qremergencias.core.mapper.Mapper;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Supplier;

import static ar.com.utn.proyecto.qremergencias.core.mapper.Converters.addTimeConverter;
import static ar.com.utn.proyecto.qremergencias.core.mapper.Converters.listConverter;
import static java.time.LocalTime.MIDNIGHT;

@Data
@NoArgsConstructor
public class EmergencyDataDTO {

    private static final Mapper<EmergencyData, EmergencyDataDTO> EMERGENCY_DATA_DTO_MAPPER =
            Mapper.mapping(EmergencyData.class, EmergencyDataDTO.class)
                    .constructor((Supplier<EmergencyDataDTO>) EmergencyDataDTO::new)
                    .fields(EmergencyData::getGeneral, EmergencyDataDTO::setGeneral, GeneralDataDTO.GENERAL_DATA_DTO_MAPPER)
                    .fields(EmergencyData::getSurgeries, EmergencyDataDTO::setSurgeries, listConverter(HospitalizationDTO.HOSPITALIZATION_DTO_MAPPER))
                    .fields(EmergencyData::getHospitalizations, EmergencyDataDTO::setHospitalizations, listConverter(HospitalizationDTO.HOSPITALIZATION_DTO_MAPPER))
                    .fields(EmergencyData::getMedications, EmergencyDataDTO::setMedications, listConverter(MedicationDTO.MEDICATION_DTO_MAPPER))
                    .fields(EmergencyData::getPathologies, EmergencyDataDTO::setPathologies, listConverter(PathologyDTO.PATHOLOGY_DTO_MAPPER))
                    .fields(EmergencyData::getLastMedicalCheck, EmergencyDataDTO::setLastMedicalCheck, addTimeConverter(MIDNIGHT));

    private GeneralDataDTO general;
    private List<HospitalizationDTO> surgeries;
    private List<HospitalizationDTO> hospitalizations;
    private List<MedicationDTO> medications;
    private List<PathologyDTO> pathologies;
    private LocalDateTime lastMedicalCheck;

    public EmergencyDataDTO(final EmergencyData emergencyData) {
        EMERGENCY_DATA_DTO_MAPPER.apply(emergencyData, this);
    }
}
