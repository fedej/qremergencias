package ar.com.utn.proyecto.qremergencias.core.repository;

import ar.com.utn.proyecto.qremergencias.core.domain.EmergencyData;
import ar.com.utn.proyecto.qremergencias.core.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface EmergencyDataRepository extends MongoRepository<EmergencyData, String> {

    Page<EmergencyData> findByUser(User user, Pageable page);
}
