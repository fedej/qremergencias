package ar.com.utn.proyecto.qremergencias.core.repository;

import ar.com.utn.proyecto.qremergencias.core.domain.DoctorFront;
import ar.com.utn.proyecto.qremergencias.core.domain.UserFront;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;

public interface UserFrontRepository extends MongoRepository<UserFront, String> {

    UserFront findByUsername(String username);

    Page<DoctorFront> findByRolesContaining(@Param("role") String role, Pageable page);

}
