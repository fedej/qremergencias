package ar.com.utn.proyecto.qremergencias.ws.service;

import ar.com.utn.proyecto.qremergencias.core.domain.EmergencyData;
import ar.com.utn.proyecto.qremergencias.core.domain.User;
import ar.com.utn.proyecto.qremergencias.core.domain.UserFront;
import ar.com.utn.proyecto.qremergencias.core.dto.EmergencyDataDTO;
import ar.com.utn.proyecto.qremergencias.core.mapper.Mapper;
import ar.com.utn.proyecto.qremergencias.core.repository.EmergencyDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class EmergencyDataService {

    private static final Mapper<EmergencyDataDTO, EmergencyData> EMERGENCY_DATA_MAPPER =
        Mapper.mapping(EmergencyDataDTO.class, EmergencyData.class)
            .constructor(EmergencyData::new)
            .fields(EmergencyDataDTO::getAge, EmergencyData::setAge)
            .fields(EmergencyDataDTO::getSex, EmergencyData::setSex)
            .fields(EmergencyDataDTO::getBloodType, EmergencyData::setBloodType)
            .fields(EmergencyDataDTO::isOrganDonor, EmergencyData::setOrganDonor)
            .fields(EmergencyDataDTO::isHypertension, EmergencyData::setHypertension)
            .fields(EmergencyDataDTO::isHeartAttack, EmergencyData::setHeartAttack)
            .fields(EmergencyDataDTO::isExtend, EmergencyData::setExtend)
            .fields(EmergencyDataDTO::isThrombosis, EmergencyData::setThrombosis)
            .fields(EmergencyDataDTO::isAsthma, EmergencyData::setAsthma)
            .fields(EmergencyDataDTO::isPneumonia, EmergencyData::setPneumonia)
            .fields(EmergencyDataDTO::isErectileDysfunction, EmergencyData::setErectileDysfunction)
            .fields(EmergencyDataDTO::isAnemia, EmergencyData::setAnemia)
            .fields(EmergencyDataDTO::isPsoriasis, EmergencyData::setPsoriasis)
            .fields(EmergencyDataDTO::isSmoking, EmergencyData::setSmoking)
            .fields(EmergencyDataDTO::isAlcoholism, EmergencyData::setAlcoholism)
            .fields(EmergencyDataDTO::isAdrenalInsufficiency, EmergencyData::setAdrenalInsufficiency)
            .fields(EmergencyDataDTO::getAllergies, EmergencyData::setAllergies)
            .fields(EmergencyDataDTO::getSurgeries, EmergencyData::setSurgeries)
            .fields(EmergencyDataDTO::getHospitalizations, EmergencyData::setHospitalizations)
            .fields(EmergencyDataDTO::getMedications, EmergencyData::setMedications)
            .fields(EmergencyDataDTO::getLastMedicalCheck, EmergencyData::setLastMedicalCheck);

    private final EmergencyDataRepository repository;

    @Autowired
    public EmergencyDataService(final EmergencyDataRepository repository) {
        this.repository = repository;
    }

    public void save(final UserFront user, final EmergencyDataDTO emergencyDataDTO) {
        final EmergencyData emergencyData = EMERGENCY_DATA_MAPPER.apply(emergencyDataDTO);
        emergencyData.setUser(user);
        repository.save(emergencyData);
    }

    public Page<EmergencyData> findByUser(final User user, final Pageable page) {
        return repository.findByUser(user, page);
    }
}
