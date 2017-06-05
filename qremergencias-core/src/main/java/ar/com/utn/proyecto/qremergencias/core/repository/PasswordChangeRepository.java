package ar.com.utn.proyecto.qremergencias.core.repository;

import ar.com.utn.proyecto.qremergencias.core.domain.PasswordChange;
import ar.com.utn.proyecto.qremergencias.core.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PasswordChangeRepository extends MongoRepository<PasswordChange, Long> {

    Page<PasswordChange> findByUser(User user, Pageable pageable);

}
