package ar.com.utn.proyecto.qremergencias.ws.service;

import ar.com.utn.proyecto.qremergencias.core.domain.User;
import ar.com.utn.proyecto.qremergencias.core.domain.UserFront;
import ar.com.utn.proyecto.qremergencias.core.domain.emergency.EmergencyData;
import ar.com.utn.proyecto.qremergencias.core.dto.emergency.EmergencyDataDTO;
import ar.com.utn.proyecto.qremergencias.core.repository.EmergencyDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class EmergencyDataService {

    private final EmergencyDataRepository repository;

    @Autowired
    public EmergencyDataService(final EmergencyDataRepository repository) {
        this.repository = repository;
    }

    public void save(final UserFront user, final EmergencyDataDTO emergencyDataDTO) {
        final EmergencyData emergencyData = DomainMappers.EMERGENCY_DATA_MAPPER.apply(emergencyDataDTO);
        emergencyData.setUser(user);
        repository.save(emergencyData);
    }

    public Page<EmergencyData> findByUser(final User user, final Pageable page) {
        return repository.findByUser(user, page);
    }

    public EmergencyData findByUser(final UserFront user) {
        return repository.findByUser(user);
    }
}
