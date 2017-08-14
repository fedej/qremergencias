package ar.com.utn.proyecto.qremergencias.ws.controller;

import ar.com.utn.proyecto.qremergencias.core.domain.UserFront;
import ar.com.utn.proyecto.qremergencias.core.domain.emergency.EmergencyData;
import ar.com.utn.proyecto.qremergencias.core.dto.emergency.EmergencyDataDTO;
import ar.com.utn.proyecto.qremergencias.ws.service.EmergencyDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Optional;

@RestController
@RequestMapping("/api/emergencyData")
public class EmergencyDataController {

    private final EmergencyDataService service;

    @Autowired
    public EmergencyDataController(final EmergencyDataService service) {
        this.service = service;
    }

    @PatchMapping
    @ResponseStatus(HttpStatus.OK)
    public void updateEmergencyData(@Valid @RequestBody final EmergencyDataDTO emergencyDataDTO,
                                    @AuthenticationPrincipal final UserFront user) {
        service.createOrUpdate(user, emergencyDataDTO);
    }

    @GetMapping
    @PreAuthorize("isFullyAuthenticated()")
    public EmergencyDataDTO getEmergencyData(@AuthenticationPrincipal final UserFront user) {
        final Optional<EmergencyData> emergencyData = service.findByUser(user);
        return new EmergencyDataDTO(emergencyData.orElse(new EmergencyData()));
    }

}
