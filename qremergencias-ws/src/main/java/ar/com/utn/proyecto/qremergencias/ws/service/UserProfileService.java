package ar.com.utn.proyecto.qremergencias.ws.service;

import ar.com.utn.proyecto.qremergencias.core.domain.User;
import ar.com.utn.proyecto.qremergencias.core.domain.UserEmergencyContact;
import ar.com.utn.proyecto.qremergencias.core.domain.UserFront;
import ar.com.utn.proyecto.qremergencias.core.dto.UserContactDTO;
import ar.com.utn.proyecto.qremergencias.core.dto.UserProfileDTO;
import ar.com.utn.proyecto.qremergencias.core.repository.UserFrontRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
public class UserProfileService {

    @Autowired
    private UserFrontRepository userFrontRepository;

    public UserProfileDTO findByUser(final User user) {
        final UserFront userFront = userFrontRepository.findByUsername(user.getUsername());
        final UserProfileDTO userProfileDTO = new UserProfileDTO();
        userProfileDTO.setFirstName(userFront.getName());
        userProfileDTO.setLastName(userFront.getLastname());
        userProfileDTO.setBirthDate(userFront.getBirthdate());
        userProfileDTO.setDocNumber(userFront.getNumeroDocumento());
        for (final UserEmergencyContact contact : userFront.getContacts()) {
            userProfileDTO.getContacts()
                    .add(
                            new UserContactDTO(
                                    contact.getId(),
                                    contact.getFirstName(),
                                    contact.getLastName(),
                                    contact.getPhoneNumber()));
        }
        return userProfileDTO;
    }

    public void update(final User user, final UserProfileDTO userProfileDTO) {
        final UserFront userFront = userFrontRepository.findByUsername(user.getUsername());
        userFront.setBirthdate(userProfileDTO.getBirthDate());
        userFront.setNumeroDocumento(userProfileDTO.getDocNumber());
        userFront.setName(userProfileDTO.getFirstName());
        userFront.setLastname(userProfileDTO.getLastName());
        final List<UserEmergencyContact> contacts = new ArrayList<>();
        for (final UserContactDTO contactDTO : userProfileDTO.getContacts()) {
            contacts.add(
                    new UserEmergencyContact(
                            contactDTO.getFirstName(),
                            contactDTO.getLastName(),
                            contactDTO.getPhoneNumber(),
                            user));
        }
        userFront.setContacts(contacts);
        userFrontRepository.save(userFront);
    }
}