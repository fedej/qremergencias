package ar.com.utn.proyecto.qremergencias.core.repository;

import ar.com.utn.proyecto.qremergencias.core.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends MongoRepository<User, String> {

    User findByUsername(String username);

    User findByUsernameAndEmail(String username, String email);

    Long countByEmail(String email);

    List<User> findByRolesContaining(@Param("role") String role);

    Page<User> findByRolesContaining(@Param("role") String role, Pageable page);

}
