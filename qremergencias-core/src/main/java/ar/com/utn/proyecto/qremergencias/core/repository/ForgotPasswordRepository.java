package ar.com.utn.proyecto.qremergencias.core.repository;

import ar.com.utn.proyecto.qremergencias.core.domain.ForgotPassword;
import ar.com.utn.proyecto.qremergencias.core.domain.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ForgotPasswordRepository extends MongoRepository<ForgotPassword, Long> {

    ForgotPassword findByUserAndExpiredFalse(User user);

    ForgotPassword findByToken(String token);
}
