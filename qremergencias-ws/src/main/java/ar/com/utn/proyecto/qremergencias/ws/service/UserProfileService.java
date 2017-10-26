package ar.com.utn.proyecto.qremergencias.ws.service;

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
@SuppressWarnings({"PMD.AvoidInstantiatingObjectsInLoops", "PMD.DataflowAnomalyAnalysis"})
public class UserProfileService {

    @Autowired
    private UserFrontRepository userFrontRepository;

    public UserFront update(final UserFront userFront, final UserProfileDTO userProfileDTO) {
        final UserFront toUpdate = userFrontRepository.findByUsername(userFront.getUsername());
        toUpdate.setBirthdate(userProfileDTO.getBirthDate().toLocalDate());
        toUpdate.setIdNumber(userProfileDTO.getIdNumber());
        toUpdate.setName(userProfileDTO.getFirstName());
        toUpdate.setLastname(userProfileDTO.getLastName());
        toUpdate.setSex(userProfileDTO.getSex());

        if (userProfileDTO.getContacts() != null) {
            final List<UserEmergencyContact> contacts = new ArrayList<>();

            for (final UserContactDTO contactDTO : userProfileDTO.getContacts()) {
                final UserEmergencyContact contact = new UserEmergencyContact(
                        contactDTO.getFirstName(),
                        contactDTO.getLastName(),
                        contactDTO.getPhoneNumber());
                contacts.add(contact);
            }
            toUpdate.setContacts(contacts);
        }
        return userFrontRepository.save(toUpdate);
    }
}
