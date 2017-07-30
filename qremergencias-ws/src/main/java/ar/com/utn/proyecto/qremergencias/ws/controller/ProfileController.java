package ar.com.utn.proyecto.qremergencias.ws.controller;

import ar.com.utn.proyecto.qremergencias.core.domain.User;
import ar.com.utn.proyecto.qremergencias.core.domain.UserEmergencyContact;
import ar.com.utn.proyecto.qremergencias.core.domain.UserFront;
import ar.com.utn.proyecto.qremergencias.core.dto.UserContactDTO;
import ar.com.utn.proyecto.qremergencias.core.dto.UserProfileDTO;
import ar.com.utn.proyecto.qremergencias.ws.service.UserProfileService;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
@Log
public class ProfileController {

    @Autowired
    private UserProfileService userProfileService;

    @GetMapping
    @PreAuthorize("isFullyAuthenticated()")
    public UserProfileDTO list(@AuthenticationPrincipal final UserFront userFront) {
        log.info("In ProfileController.list()");
        final UserProfileDTO userProfileDTO = new UserProfileDTO();
        userProfileDTO.setFirstName(userFront.getName());
        userProfileDTO.setLastName(userFront.getLastname());
        userProfileDTO.setBirthDate(userFront.getBirthdate());
        userProfileDTO.setDocNumber(userFront.getNumeroDocumento());
        for (final UserEmergencyContact contact : userFront.getContacts()) {
            userProfileDTO.getContacts()
                    .add(
                            new UserContactDTO(
                                    contact.getFirstName(),
                                    contact.getLastName(),
                                    contact.getPhoneNumber()));
        }
        return userProfileDTO;
    }

    @PatchMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isFullyAuthenticated()")
    public void update(@RequestBody final UserProfileDTO userProfileDTO,
            @AuthenticationPrincipal final UserFront user) {
        log.info("In ProfileController.update()");
        userProfileService.update(user, userProfileDTO);
    }

}
