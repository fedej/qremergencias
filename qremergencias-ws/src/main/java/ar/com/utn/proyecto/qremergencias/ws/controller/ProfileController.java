package ar.com.utn.proyecto.qremergencias.ws.controller;

import ar.com.utn.proyecto.qremergencias.core.domain.User;
import ar.com.utn.proyecto.qremergencias.core.dto.UserProfileDTO;
import ar.com.utn.proyecto.qremergencias.ws.service.UserProfileService;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
@Log
public class ProfileController {

    @Autowired
    private UserProfileService emergencyDataService;

    @GetMapping
    @PreAuthorize("isFullyAuthenticated()")
    public UserProfileDTO list(@AuthenticationPrincipal final User user) {
        log.info("Llamado a ");
        return emergencyDataService.findByUser(user);
    }

    @PatchMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isFullyAuthenticated()")
    public void update(final UserProfileDTO userProfileDTO,
            @AuthenticationPrincipal final User user) {
        emergencyDataService.update(user, userProfileDTO);
    }

}
