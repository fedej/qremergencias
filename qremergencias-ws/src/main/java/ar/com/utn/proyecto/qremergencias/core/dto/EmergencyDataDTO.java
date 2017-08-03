package ar.com.utn.proyecto.qremergencias.core.dto;

import ar.com.utn.proyecto.qremergencias.core.domain.EmergencyData;
import ar.com.utn.proyecto.qremergencias.core.mapper.Mapper;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Supplier;

@Data
@NoArgsConstructor
public class EmergencyDataDTO {

    private static final Mapper<EmergencyData, EmergencyDataDTO> EMERGENCY_DATA_DTO_MAPPER =
            Mapper.mapping(EmergencyData.class, EmergencyDataDTO.class)
                    .constructor((Supplier<EmergencyDataDTO>) EmergencyDataDTO::new)
                    .fields(EmergencyData::getAge, EmergencyDataDTO::setAge)
                    .fields(EmergencyData::getSex, EmergencyDataDTO::setSex)
                    .fields(EmergencyData::getBloodType, EmergencyDataDTO::setBloodType)
                    .fields(EmergencyData::isOrganDonor, EmergencyDataDTO::setOrganDonor)
                    .fields(EmergencyData::isHypertension, EmergencyDataDTO::setHypertension)
                    .fields(EmergencyData::isHeartAttack, EmergencyDataDTO::setHeartAttack)
                    .fields(EmergencyData::isExtend, EmergencyDataDTO::setExtend)
                    .fields(EmergencyData::isThrombosis, EmergencyDataDTO::setThrombosis)
                    .fields(EmergencyData::isAsthma, EmergencyDataDTO::setAsthma)
                    .fields(EmergencyData::isPneumonia, EmergencyDataDTO::setPneumonia)
                    .fields(EmergencyData::isErectileDysfunction, EmergencyDataDTO::setErectileDysfunction)
                    .fields(EmergencyData::isAnemia, EmergencyDataDTO::setAnemia)
                    .fields(EmergencyData::isPsoriasis, EmergencyDataDTO::setPsoriasis)
                    .fields(EmergencyData::isSmoking, EmergencyDataDTO::setSmoking)
                    .fields(EmergencyData::isAlcoholism, EmergencyDataDTO::setAlcoholism)
                    .fields(EmergencyData::isAdrenalInsufficiency, EmergencyDataDTO::setAdrenalInsufficiency)
                    .fields(EmergencyData::getAllergies, EmergencyDataDTO::setAllergies)
                    .fields(EmergencyData::getSurgeries, EmergencyDataDTO::setSurgeries)
                    .fields(EmergencyData::getHospitalizations, EmergencyDataDTO::setHospitalizations)
                    .fields(EmergencyData::getMedications, EmergencyDataDTO::setMedications)
                    .fields(EmergencyData::getLastMedicalCheck, EmergencyDataDTO::setLastMedicalCheck);

    @Min(0)
    @Max(150)
    private int age;


    @Length(min = 1, max = 3)
    private String bloodType;

    private char sex;
    private boolean organDonor;
    private boolean hypertension;
    private boolean heartAttack;
    private boolean extend;
    private boolean thrombosis;
    private boolean asthma;
    private boolean pneumonia;
    private boolean erectileDysfunction;
    private boolean anemia;
    private boolean psoriasis;
    private boolean smoking;
    private boolean alcoholism;
    private boolean adrenalInsufficiency;
    private List<String> allergies;
    private List<String> surgeries;
    private List<String> hospitalizations;
    private List<String> medications;
    private LocalDate lastMedicalCheck;

    public EmergencyDataDTO(final EmergencyData emergencyData) {
        EMERGENCY_DATA_DTO_MAPPER.apply(emergencyData, this);
    }
}
