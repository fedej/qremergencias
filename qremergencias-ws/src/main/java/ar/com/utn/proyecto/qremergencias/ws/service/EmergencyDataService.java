package ar.com.utn.proyecto.qremergencias.ws.service;

import ar.com.utn.proyecto.qremergencias.core.domain.UserFront;
import ar.com.utn.proyecto.qremergencias.core.domain.emergency.EmergencyData;
import ar.com.utn.proyecto.qremergencias.core.dto.emergency.EmergencyDataDTO;
import ar.com.utn.proyecto.qremergencias.core.repository.EmergencyDataRepository;
import ar.com.utn.proyecto.qremergencias.core.repository.UserFrontRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static ar.com.utn.proyecto.qremergencias.ws.service.DomainMappers.EMERGENCY_DATA_MAPPER;

@Service
public class EmergencyDataService {

    private final EmergencyDataRepository repository;
    private final UserFrontRepository userFrontRepository;

    @Autowired
    public EmergencyDataService(final EmergencyDataRepository repository,
                                final UserFrontRepository userFrontRepository) {
        this.repository = repository;
        this.userFrontRepository = userFrontRepository;
    }

    public Optional<EmergencyData> findByUser(final String username) {
        final UserFront user = userFrontRepository.findByUsername(username);
        return repository.findByUser(user);
    }

    public void createOrUpdate(final String username, final EmergencyDataDTO emergencyDataDTO) {
        final UserFront user = userFrontRepository.findByUsername(username);
        final Optional<EmergencyData> oldData = repository.findByUser(user);

        if (oldData.isPresent()) {
            final EmergencyData emergencyData = EMERGENCY_DATA_MAPPER.apply(emergencyDataDTO, oldData.get());
            repository.save(emergencyData);
        } else {
            final EmergencyData emergencyData = EMERGENCY_DATA_MAPPER.apply(emergencyDataDTO);
            emergencyData.setUser(user);
            repository.save(emergencyData);
        }

    }
}
