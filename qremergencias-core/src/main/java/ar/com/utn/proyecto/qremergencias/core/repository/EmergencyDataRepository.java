package ar.com.utn.proyecto.qremergencias.core.repository;

import ar.com.utn.proyecto.qremergencias.core.domain.UserFront;
import ar.com.utn.proyecto.qremergencias.core.domain.emergency.EmergencyData;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

@JaversSpringDataAuditable
public interface EmergencyDataRepository extends MongoRepository<EmergencyData, String> {

    Optional<EmergencyData> findByUser(UserFront user);

    Optional<EmergencyData> findByUuid(String uuid);
}
