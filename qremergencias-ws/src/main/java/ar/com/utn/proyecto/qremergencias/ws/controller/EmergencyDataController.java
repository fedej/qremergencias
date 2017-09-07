package ar.com.utn.proyecto.qremergencias.ws.controller;

import ar.com.utn.proyecto.qremergencias.core.domain.UserFront;
import ar.com.utn.proyecto.qremergencias.core.domain.emergency.EmergencyData;
import ar.com.utn.proyecto.qremergencias.core.dto.emergency.EmergencyDataDTO;
import ar.com.utn.proyecto.qremergencias.core.dto.emergency.changelog.ChangesDTO;
import ar.com.utn.proyecto.qremergencias.ws.service.EmergencyDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    @PreAuthorize("hasRole('MEDICO')")
    public void updateEmergencyData(@Valid @RequestBody final EmergencyDataDTO emergencyDataDTO,
                                    @RequestParam final String userId) {
        service.createOrUpdate(userId, emergencyDataDTO);
    }

    @GetMapping
    @PreAuthorize("isFullyAuthenticated()")
    public EmergencyDataDTO getEmergencyData(@RequestParam final String userId) {
        final Optional<EmergencyData> emergencyData = service.findByUser(userId);
        return new EmergencyDataDTO(emergencyData.orElse(new EmergencyData()));
    }

    @GetMapping("/changes")
    @PreAuthorize("hasRole('PACIENTE')")
    public Page<ChangesDTO> getChanges(@AuthenticationPrincipal final UserFront user) {
        return service.getEmergencyDataChanges(user);
    }

}
