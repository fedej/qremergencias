package ar.com.utn.proyecto.qremergencias.ws.controller;

import ar.com.utn.proyecto.qremergencias.core.domain.UserFront;
import ar.com.utn.proyecto.qremergencias.core.domain.emergency.EmergencyData;
import ar.com.utn.proyecto.qremergencias.core.dto.emergency.EmergencyDataDTO;
import ar.com.utn.proyecto.qremergencias.core.dto.emergency.changelog.ChangesDTO;
import ar.com.utn.proyecto.qremergencias.util.CryptoUtils;
import ar.com.utn.proyecto.qremergencias.ws.exceptions.PequeniaLisaException;
import ar.com.utn.proyecto.qremergencias.ws.service.EmergencyDataService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.io.IOException;
import java.util.Optional;

@RestController
@RequestMapping("/api/emergencyData")
public class EmergencyDataController {

    private final EmergencyDataService service;
    private static final String CHARSET_NAME = "ISO-8859-1";

    @Autowired
    private ObjectMapper oMapper;

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

    @GetMapping("/{uuid}")
    public String getEmergencyDataByUuid(@PathVariable final String uuid) throws PequeniaLisaException {
        final Optional<EmergencyData> emergencyData = service.findByUuid(uuid);
        EmergencyDataDTO emergencyDataDTO = new EmergencyDataDTO(emergencyData.orElse(new EmergencyData()));
        try {
            String emergencyDTOString = oMapper.writeValueAsString(emergencyDataDTO);
            return CryptoUtils.encryptText(emergencyDTOString.getBytes(CHARSET_NAME)) ;
        }catch (Exception e) {
            throw new PequeniaLisaException(e);
        }
    }

    @GetMapping("/qr")
    public ResponseEntity<Resource> getQR(@RequestParam(name = "user") final String user) {
        final Resource userQR = service.getUserQR(user);
        return userQR == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(userQR);
    }

    @PostMapping("/qr")
    @PreAuthorize("hasRole('PACIENTE')")
    public void createQR(@AuthenticationPrincipal final UserFront user) {
        service.createQR(user.getUsername());
    }

    @DeleteMapping("/qr")
    @PreAuthorize("hasRole('PACIENTE')")
    public void deleteQR(@AuthenticationPrincipal final UserFront user) {
        service.deleteQR(user.getUsername());
    }


}
