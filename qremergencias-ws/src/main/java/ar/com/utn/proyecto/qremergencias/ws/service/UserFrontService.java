package ar.com.utn.proyecto.qremergencias.ws.service;

import ar.com.utn.proyecto.qremergencias.core.domain.Role;
import ar.com.utn.proyecto.qremergencias.core.domain.UserFront;
import ar.com.utn.proyecto.qremergencias.core.dto.CreateUserDTO;
import ar.com.utn.proyecto.qremergencias.core.repository.RoleRepository;
import ar.com.utn.proyecto.qremergencias.core.repository.UserFrontRepository;
import ar.com.utn.proyecto.qremergencias.core.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserFrontService extends UserService {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserFrontRepository userFrontRepository;

    public UserFront create(final CreateUserDTO createUserDTO) {
        if (createUserDTO == null || !createUserDTO.isTyc()) {
            return null;
        }
        final UserFront user = new UserFront();
        user.setUsername(createUserDTO.getEmail());
        user.setEmail(createUserDTO.getEmail());
        user.setPassword(createUserDTO.getPassword());
        user.getRoles().add(roleRepository.findByAuthority(Role.ROLE_USER));
        user.setName(createUserDTO.getName());
        user.setLastname(createUserDTO.getLastname());
        return save(user);

    }

    public UserFront findByUsername(final String username) {
        return userFrontRepository.findByUsername(username);
    }

}
