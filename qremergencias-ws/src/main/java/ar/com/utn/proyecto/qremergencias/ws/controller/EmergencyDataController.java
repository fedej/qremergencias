package ar.com.utn.proyecto.qremergencias.ws.controller;

import ar.com.utn.proyecto.qremergencias.core.domain.emergency.EmergencyData;
import ar.com.utn.proyecto.qremergencias.core.domain.UserFront;
import ar.com.utn.proyecto.qremergencias.core.dto.emergency.EmergencyDataDTO;
import ar.com.utn.proyecto.qremergencias.ws.service.EmergencyDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/emergencyData")
public class EmergencyDataController {

    private final EmergencyDataService service;

    @Autowired
    public EmergencyDataController(final EmergencyDataService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@Valid @RequestBody final EmergencyDataDTO emergencyDataDTO,
                       @AuthenticationPrincipal final UserFront user) {
        service.save(user, emergencyDataDTO);
    }

    @GetMapping
    @PreAuthorize("isFullyAuthenticated()")
    public Page<EmergencyDataDTO> list(@PageableDefault final Pageable page,
                                       @AuthenticationPrincipal final UserFront user) {

        final Page<EmergencyData> domainPage = service.findByUser(user, page);
        return domainPage.map(EmergencyDataDTO::new);
    }

}
