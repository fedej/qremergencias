package ar.com.utn.proyecto.qremergencias.ws.controller;

import ar.com.utn.proyecto.qremergencias.core.domain.User;
import ar.com.utn.proyecto.qremergencias.core.domain.UserEmergencyContact;
import ar.com.utn.proyecto.qremergencias.core.domain.UserFront;
import ar.com.utn.proyecto.qremergencias.core.dto.UserContactDTO;
import ar.com.utn.proyecto.qremergencias.ws.service.EmergencyDataService;
import ar.com.utn.proyecto.qremergencias.ws.service.UserFrontService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/emergency")
public class EmergencyDataController {

    @Autowired
    private EmergencyDataService emergencyDataService;

    @Autowired
    private UserFrontService userFrontService;

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public UserFront getUserData(@PathVariable final String id) {
        User userFound = userFrontService.findById(id);
        return userFrontService.findByUsername(userFound.getUsername());
    }

    @RequestMapping(value = "/{id}/contacts", method = RequestMethod.GET)
    public List<UserEmergencyContact> getUserContacts(@PathVariable final String id) {
        return emergencyDataService.findContactsByUserId(id);
    }

    @RequestMapping(value = "/{id}/contacts", method = RequestMethod.POST, consumes = "application/json")
    public void postUserContact(@PathVariable String id, @RequestBody UserContactDTO userContactDTO) {
        User userFound = userFrontService.findById(id);
        UserFront userFront = userFrontService.findByUsername(userFound.getUsername());
        UserEmergencyContact contact = new UserEmergencyContact();
        contact.setFirstName(userContactDTO.getFirstName());
        contact.setLastName(userContactDTO.getLastName());
        contact.setPhoneNumber(userContactDTO.getPhoneNumber());
        emergencyDataService.saveContact(userFront, contact);
    }


}
