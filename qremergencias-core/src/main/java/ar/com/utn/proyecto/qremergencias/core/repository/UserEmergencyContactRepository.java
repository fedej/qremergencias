package ar.com.utn.proyecto.qremergencias.core.repository;

import ar.com.utn.proyecto.qremergencias.core.domain.UserEmergencyContact;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserEmergencyContactRepository
        extends MongoRepository<UserEmergencyContact, String> {

}
