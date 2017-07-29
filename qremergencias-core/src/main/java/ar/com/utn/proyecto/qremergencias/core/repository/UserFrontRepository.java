package ar.com.utn.proyecto.qremergencias.core.repository;

import ar.com.utn.proyecto.qremergencias.core.domain.UserFront;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserFrontRepository extends MongoRepository<UserFront, String> {

    UserFront findByUsername(String username);

}
