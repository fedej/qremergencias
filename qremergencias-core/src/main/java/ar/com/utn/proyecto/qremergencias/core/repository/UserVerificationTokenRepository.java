package ar.com.utn.proyecto.qremergencias.core.repository;

import ar.com.utn.proyecto.qremergencias.core.domain.User;
import ar.com.utn.proyecto.qremergencias.core.domain.UserVerificationToken;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Created by yoga on 26/06/17.
 */
public interface UserVerificationTokenRepository
        extends MongoRepository<UserVerificationToken, String> {

    UserVerificationToken findByToken(String token);

    UserVerificationToken findByUser(User user);

}
