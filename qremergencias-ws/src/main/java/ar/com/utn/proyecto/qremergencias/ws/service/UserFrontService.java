package ar.com.utn.proyecto.qremergencias.ws.service;

import ar.com.utn.proyecto.qremergencias.core.domain.UserFront;
import ar.com.utn.proyecto.qremergencias.core.dto.CreateUserDTO;
import ar.com.utn.proyecto.qremergencias.core.repository.UserFrontRepository;
import ar.com.utn.proyecto.qremergencias.core.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserFrontService extends UserService {

    @Autowired
    private UserFrontRepository userFrontRepository;

    public UserFront create(final CreateUserDTO createUserDTO) {
        if (createUserDTO == null) {
            return null;
        }
        final UserFront user = new UserFront();
        user.setUsername(createUserDTO.getEmail());
        user.setEmail(createUserDTO.getEmail());
        user.setPassword(createUserDTO.getPassword());
        user.getRoles().add(createUserDTO.getRole());
        final UserFront userFront = save(user);
        final String token = UUID.randomUUID().toString();
        createVerificationToken(userFront,token);
        return userFront;

    }

    public UserFront findByUsername(final String username) {
        return userFrontRepository.findByUsername(username);
    }

    public UserFront update(final UserFront toUpdate) {
        return userFrontRepository.save(toUpdate);
    }

}
