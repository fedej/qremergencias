package ar.com.utn.proyecto.qremergencias.ws.service;

import ar.com.utn.proyecto.qremergencias.core.domain.emergency.EmergencyData;
import ar.com.utn.proyecto.qremergencias.core.domain.emergency.GeneralData;
import ar.com.utn.proyecto.qremergencias.core.domain.emergency.Hospitalization;
import ar.com.utn.proyecto.qremergencias.core.domain.emergency.Medication;
import ar.com.utn.proyecto.qremergencias.core.domain.emergency.Pathology;
import ar.com.utn.proyecto.qremergencias.core.dto.emergency.EmergencyDataDTO;
import ar.com.utn.proyecto.qremergencias.core.dto.emergency.GeneralDataDTO;
import ar.com.utn.proyecto.qremergencias.core.dto.emergency.HospitalizationDTO;
import ar.com.utn.proyecto.qremergencias.core.dto.emergency.MedicationDTO;
import ar.com.utn.proyecto.qremergencias.core.dto.emergency.PathologyDTO;
import ar.com.utn.proyecto.qremergencias.core.mapper.Mapper;

import java.time.LocalDate;

import static ar.com.utn.proyecto.qremergencias.core.mapper.Converters.listConverter;
import static ar.com.utn.proyecto.qremergencias.core.mapper.Converters.localDateConverter;

class DomainMappers {

    private static final Mapper<GeneralDataDTO, GeneralData> GENERAL_DATA_MAPPER =
            Mapper.mapping(GeneralDataDTO.class, GeneralData.class)
                    .constructor(GeneralData::new)
                    .fields(GeneralDataDTO::getBloodType, GeneralData::setBloodType)
                    .fields(GeneralDataDTO::isOrganDonor, GeneralData::setOrganDonor)
                    .fields(GeneralDataDTO::getAllergies, GeneralData::setAllergies)
                    .fields((d) -> LocalDate.now(), GeneralData::setLastMedicalCheck);

    private static final Mapper<HospitalizationDTO, Hospitalization> HOSPITALIZATION_MAPPER =
            Mapper.mapping(HospitalizationDTO.class, Hospitalization.class)
                    .constructor(Hospitalization::new)
                    .fields(HospitalizationDTO::getInstitution, Hospitalization::setInstitution)
                    .fields(HospitalizationDTO::getType, Hospitalization::setType, HospitalizationDTO.Type::name)
                    .fields(HospitalizationDTO::getDate, Hospitalization::setDate, localDateConverter())
                    .fields(HospitalizationDTO::getDate, Hospitalization::setDate, localDateConverter())
                    .fields(HospitalizationDTO::getReason, Hospitalization::setReason);

    private static final Mapper<MedicationDTO, Medication> MEDICATION_MAPPER =
            Mapper.mapping(MedicationDTO.class, Medication.class)
                    .constructor(Medication::new)
                    .fields(MedicationDTO::getName, Medication::setName)
                    .fields(MedicationDTO::getDescription, Medication::setDescription)
                    .fields(MedicationDTO::getAmount, Medication::setAmount)
                    .fields(MedicationDTO::getPeriod, Medication::setPeriod, MedicationDTO.Period::name);

    private static final Mapper<PathologyDTO, Pathology> PATHOLOGY_DTO_MAPPER =
            Mapper.mapping(PathologyDTO.class, Pathology.class)
                    .constructor(Pathology::new)
                    .fields(PathologyDTO::getDescription, Pathology::setDescription)
                    .fields(PathologyDTO::getType, Pathology::setType, PathologyDTO.Type::name)
                    .fields(PathologyDTO::getDate, Pathology::setDate, localDateConverter());

    public static final Mapper<EmergencyDataDTO, EmergencyData> EMERGENCY_DATA_MAPPER =
            Mapper.mapping(EmergencyDataDTO.class, EmergencyData.class)
                    .constructor(EmergencyData::new)
                    .fields(EmergencyDataDTO::getUuid, EmergencyData::setUuid)
                    .fields(EmergencyDataDTO::getGeneral, EmergencyData::setGeneral, GENERAL_DATA_MAPPER)
                    .fields(EmergencyDataDTO::getHospitalizations, EmergencyData::setHospitalizations,
                            listConverter(HOSPITALIZATION_MAPPER))
                    .fields(EmergencyDataDTO::getSurgeries, EmergencyData::setSurgeries,
                            listConverter(HOSPITALIZATION_MAPPER))
                    .fields(EmergencyDataDTO::getMedications, EmergencyData::setMedications,
                            listConverter(MEDICATION_MAPPER))
                    .fields(EmergencyDataDTO::getPathologies, EmergencyData::setPathologies,
                            listConverter(PATHOLOGY_DTO_MAPPER));

}
