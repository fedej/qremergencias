package ar.com.utn.proyecto.qremergencias.ws.service;

import ar.com.utn.proyecto.qremergencias.core.domain.User;
import ar.com.utn.proyecto.qremergencias.core.domain.UserEmergencyContact;
import ar.com.utn.proyecto.qremergencias.core.domain.UserFront;
import ar.com.utn.proyecto.qremergencias.core.repository.UserEmergencyContactRepository;
import ar.com.utn.proyecto.qremergencias.core.repository.UserFrontRepository;
import ar.com.utn.proyecto.qremergencias.core.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EmergencyDataService {

    @Autowired
    private UserFrontRepository userFrontRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserEmergencyContactRepository userEmergencyContactRepository;

    public List<UserEmergencyContact> findContactsByUserId(final String id) {
        final User user = userRepository.findOne(id);
        final UserFront userFront = userFrontRepository.findByUsername(user.getUsername());
        return userFront.getContacts();
    }

    public void saveContact(final UserFront userFront, final UserEmergencyContact contact) {
        if (userFront.getContacts() == null) {
            userFront.setContacts(new ArrayList<>());
        }
        userFront.getContacts().add(contact);
        userEmergencyContactRepository.save(contact);
        userFrontRepository.save(userFront);
    }

    public void updateContact(final UserFront userFront, final UserEmergencyContact contact) {
        userEmergencyContactRepository.save(contact);
        userFrontRepository.save(userFront);
    }

    public UserEmergencyContact findContact(final String idContact) {
        return userEmergencyContactRepository.findOne(idContact);
    }

    public void deleteContact(final UserEmergencyContact contact) {
        userEmergencyContactRepository.delete(contact);
    }

}
