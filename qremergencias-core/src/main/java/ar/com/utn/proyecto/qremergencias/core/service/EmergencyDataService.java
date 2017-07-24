package ar.com.utn.proyecto.qremergencias.core.service;

import ar.com.utn.proyecto.qremergencias.core.domain.User;
import ar.com.utn.proyecto.qremergencias.core.domain.UserEmergencyContact;
import ar.com.utn.proyecto.qremergencias.core.domain.UserFront;
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

    public List<UserEmergencyContact> findContactsByUserId(String id) {
        User user = userRepository.findOne(id);
        UserFront userFront = userFrontRepository.findByUsername(user.getUsername());
        return userFront.getContacts();
    }

    public void saveContact(UserFront userFront, UserEmergencyContact contact){
        if(userFront.getContacts()==null)
            userFront.setContacts(new ArrayList<>());
        userFront.getContacts().add(contact);
        userFrontRepository.save(userFront);
    }

}
