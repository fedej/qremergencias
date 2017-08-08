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

    public void update(final UserFront userFront, final UserProfileDTO userProfileDTO) {
        userFront.setBirthdate(userProfileDTO.getBirthDate());
        userFront.setIdNumber(userProfileDTO.getIdNumber());
        userFront.setName(userProfileDTO.getFirstName());
        userFront.setLastname(userProfileDTO.getLastName());
        userFront.setSex(userProfileDTO.getSex());

        if (userProfileDTO.getContacts() != null) {
            final List<UserEmergencyContact> contacts = new ArrayList<>();

            for (final UserContactDTO contactDTO : userProfileDTO.getContacts()) {
                final UserEmergencyContact contact = new UserEmergencyContact(
                        contactDTO.getFirstName(),
                        contactDTO.getLastName(),
                        contactDTO.getPhoneNumber());
                contacts.add(contact);
            }
            userFront.setContacts(contacts);
        }
        userFrontRepository.save(userFront);
    }
}
