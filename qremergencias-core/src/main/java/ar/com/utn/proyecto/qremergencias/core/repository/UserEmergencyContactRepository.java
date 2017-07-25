package ar.com.utn.proyecto.qremergencias.core.repository;

import ar.com.utn.proyecto.qremergencias.core.domain.User;
import ar.com.utn.proyecto.qremergencias.core.domain.UserEmergencyContact;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface UserEmergencyContactRepository
        extends MongoRepository<UserEmergencyContact, String> {

    List<UserEmergencyContact> findByUser(User user);

}
