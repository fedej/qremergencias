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

import java.util.List;

@RestController
@RequestMapping("/api/emergency")
@Log
public class EmergencyDataController {

    @Autowired
    private EmergencyDataService emergencyDataService;

    @Autowired
    private UserFrontService userFrontService;

    @RequestMapping(value = "/{id}",
            method = RequestMethod.GET)
    public UserFront getUserData(@PathVariable final String id) {
        log.info("Llamado a EmergencyDataController.getUserData");
        final User userFound = userFrontService.findById(id);
        return userFrontService.findByUsername(userFound.getUsername());
    }

    @RequestMapping(value = "/{id}/contacts",
            method = RequestMethod.GET)
    public List<UserEmergencyContact> getUserContacts(@PathVariable final String id) {
        log.info("Llamado a EmergencyDataController.getUserContacts");
        return emergencyDataService.findContactsByUserId(id);
    }

    @RequestMapping(value = "/{id}/contacts",
            method = RequestMethod.POST,
            consumes = "application/json")
    public void postUserContact(@PathVariable final String id,
                                @RequestBody final UserContactDTO userContactDTO) {
        log.info("Llamado a EmergencyDataController.postUserContact");
        final User userFound = userFrontService.findById(id);
        final UserFront userFront = userFrontService.findByUsername(userFound.getUsername());
        final UserEmergencyContact contact = new UserEmergencyContact();
        contact.setFirstName(userContactDTO.getFirstName());
        contact.setLastName(userContactDTO.getLastName());
        contact.setPhoneNumber(userContactDTO.getPhoneNumber());
        emergencyDataService.saveContact(userFront, contact);
    }

    @RequestMapping(value = "/{idUser}/contacts/{idContact}",
            method = RequestMethod.DELETE)
    public void deleteUserContact(@PathVariable final String idUser,
                                  @PathVariable final String idContact) {
        log.info("Llamado a EmergencyDataController.deleteUserContact");
        final UserEmergencyContact contact = emergencyDataService.findContact(idContact);
        emergencyDataService.deleteContact(contact);
    }

    @RequestMapping(value = "/{id}/contacts",
            method = RequestMethod.PUT,
            consumes = "application/json")
    public void updateUserContact(@PathVariable final String id,
                                  @RequestBody final UserContactDTO userContactDTO) {
        log.info("Llamado a EmergencyDataController.updateUserContact");
        final User userFound = userFrontService.findById(id);
        final UserFront userFront = userFrontService.findByUsername(userFound.getUsername());
        final UserEmergencyContact contactToUpdate =
                emergencyDataService.findContact(userContactDTO.getId());
        contactToUpdate.setFirstName(userContactDTO.getFirstName());
        contactToUpdate.setLastName(userContactDTO.getLastName());
        contactToUpdate.setPhoneNumber(userContactDTO.getPhoneNumber());
        emergencyDataService.saveContact(userFront, contactToUpdate);
    }


}
