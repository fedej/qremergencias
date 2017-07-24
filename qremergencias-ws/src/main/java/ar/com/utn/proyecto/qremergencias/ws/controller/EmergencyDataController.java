package ar.com.utn.proyecto.qremergencias.ws.controller;

import ar.com.utn.proyecto.qremergencias.core.domain.User;
import ar.com.utn.proyecto.qremergencias.core.domain.UserEmergencyContact;
import ar.com.utn.proyecto.qremergencias.core.domain.UserFront;
import ar.com.utn.proyecto.qremergencias.core.dto.UserContactDTO;
import ar.com.utn.proyecto.qremergencias.ws.service.EmergencyDataService;
import ar.com.utn.proyecto.qremergencias.ws.service.UserFrontService;
import lombok.extern.java.Log;
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
@Log
public class EmergencyDataController {

    @Autowired
    private EmergencyDataService emergencyDataService;

    @Autowired
    private UserFrontService userFrontService;

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public UserFront getUserData(@PathVariable final String id) {
        log.info("Llamado a EmergencyDataController.getUserData");
        User userFound = userFrontService.findById(id);
        return userFrontService.findByUsername(userFound.getUsername());
    }

    @RequestMapping(value = "/{id}/contacts", method = RequestMethod.GET)
    public List<UserEmergencyContact> getUserContacts(@PathVariable final String id) {
        log.info("Llamado a EmergencyDataController.getUserContacts");
        return emergencyDataService.findContactsByUserId(id);
    }

    @RequestMapping(value = "/{id}/contacts", method = RequestMethod.POST, consumes = "application/json")
    public void postUserContact(@PathVariable String id, @RequestBody UserContactDTO userContactDTO) {
        log.info("Llamado a EmergencyDataController.postUserContact");
        User userFound = userFrontService.findById(id);
        UserFront userFront = userFrontService.findByUsername(userFound.getUsername());
        UserEmergencyContact contact = new UserEmergencyContact();
        contact.setFirstName(userContactDTO.getFirstName());
        contact.setLastName(userContactDTO.getLastName());
        contact.setPhoneNumber(userContactDTO.getPhoneNumber());
        emergencyDataService.saveContact(userFront, contact);
    }

    @RequestMapping(value = "/{idUser}/contacts/{idContact}", method = RequestMethod.DELETE)
    public void deleteUserContact(@PathVariable String idUser, @PathVariable String idContact){
        log.info("Llamado a EmergencyDataController.deleteUserContact");
        User userFound = userFrontService.findById(idUser);
        UserFront userFront = userFrontService.findByUsername(userFound.getUsername());
        UserEmergencyContact contact = emergencyDataService.findContact(idContact);
        emergencyDataService.deleteContact(contact);
    }

    @RequestMapping(value = "/{id}/contacts", method = RequestMethod.PUT, consumes = "application/json")
    public void updateUserContact(@PathVariable String id, @RequestBody UserContactDTO userContactDTO) {
        log.info("Llamado a EmergencyDataController.updateUserContact");
        User userFound = userFrontService.findById(id);
        UserFront userFront = userFrontService.findByUsername(userFound.getUsername());
        UserEmergencyContact contactToUpdate = emergencyDataService.findContact(userContactDTO.getId());
        contactToUpdate.setFirstName(userContactDTO.getFirstName());
        contactToUpdate.setLastName(userContactDTO.getLastName());
        contactToUpdate.setPhoneNumber(userContactDTO.getPhoneNumber());
        emergencyDataService.saveContact(userFront, contactToUpdate);
    }


}
