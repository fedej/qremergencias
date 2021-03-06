package ar.com.utn.proyecto.qremergencias.ws.controller;

import ar.com.utn.proyecto.qremergencias.core.domain.UserFront;
import ar.com.utn.proyecto.qremergencias.core.domain.emergency.EmergencyData;
import ar.com.utn.proyecto.qremergencias.core.dto.FilterDTO;
import ar.com.utn.proyecto.qremergencias.core.dto.emergency.EmergencyDataDTO;
import ar.com.utn.proyecto.qremergencias.core.dto.emergency.changelog.ChangesDTO;
import ar.com.utn.proyecto.qremergencias.util.CryptoUtils;
import ar.com.utn.proyecto.qremergencias.ws.exceptions.InvalidQRException;
import ar.com.utn.proyecto.qremergencias.ws.exceptions.PequeniaLisaException;
import ar.com.utn.proyecto.qremergencias.ws.service.ChangesService;
import ar.com.utn.proyecto.qremergencias.ws.service.EmergencyDataService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.time.LocalDate;
import java.util.Optional;

@RestController
@RequestMapping("/api/emergencyData")
public class EmergencyDataController {

    private static final String CHARSET_NAME = "ISO-8859-1";
    private static final String HAS_ROLE_PACIENTE = "hasRole('PACIENTE')";

    private final EmergencyDataService service;
    private final ObjectMapper objectMapper;
    private final ChangesService changesService;

    @Autowired
    public EmergencyDataController(final EmergencyDataService service, final ObjectMapper objectMapper,
                                   final ChangesService changesService) {
        this.service = service;
        this.objectMapper = objectMapper;
        this.changesService = changesService;
    }

    @PatchMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('MEDICO')")
    public void updateEmergencyData(@Valid @RequestBody final EmergencyDataDTO emergencyDataDTO,
                                    @RequestParam final String userId,
                                    @RequestParam final boolean qrUpdateRequired) {
        service.createOrUpdate(userId, emergencyDataDTO, qrUpdateRequired);
    }

    @GetMapping
    @PreAuthorize("isFullyAuthenticated()")
    public EmergencyDataDTO getEmergencyData(@RequestParam final String userId) {
        final Optional<EmergencyData> emergencyData = service.findByUser(userId);
        return new EmergencyDataDTO(emergencyData.orElse(new EmergencyData()));
    }

    @GetMapping("/changes")
    @PreAuthorize(HAS_ROLE_PACIENTE)
    public Page<ChangesDTO> getChanges(@AuthenticationPrincipal final UserFront user,
                                       @PageableDefault final Pageable page,
                                       @RequestParam(required = false) final String text,
                                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                           @RequestParam(required = false) final LocalDate from,
                                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                           @RequestParam(required = false) final LocalDate to) {
        final FilterDTO filter = new FilterDTO(from, to, text);
        return changesService.getEmergencyDataChanges(user, page, filter);
    }

    @GetMapping("/{uuid}")
    public String getEmergencyDataByUuid(@PathVariable final String uuid) throws PequeniaLisaException {
        final Optional<EmergencyData> emergencyData = service.findByUuid(uuid);
        if (emergencyData.isPresent()) {
            final EmergencyData dbEmergencyData = emergencyData.get();
            final EmergencyDataDTO emergencyDataDTO = new EmergencyDataDTO(dbEmergencyData);
            try {
                final String emergencyDTOString = objectMapper.writeValueAsString(emergencyDataDTO);
                return CryptoUtils.encryptText(emergencyDTOString.getBytes(CHARSET_NAME));
            } catch (final Exception exception) {
                throw new PequeniaLisaException(exception);
            }
        } else {
            throw new InvalidQRException();
        }
    }

    @RequestMapping(value = "/qr", method = { RequestMethod.HEAD, RequestMethod.GET })
    @PreAuthorize(HAS_ROLE_PACIENTE)
    public Resource getQR(@RequestParam(name = "user") final String user, final HttpServletResponse response) {
        final Resource userQR = service.getUserQR(user);

        if (userQR == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        response.setContentType(MediaType.IMAGE_PNG_VALUE);
        return userQR;
    }

    @PostMapping("/qr")
    @PreAuthorize(HAS_ROLE_PACIENTE)
    public void createQR(@AuthenticationPrincipal final UserFront user) {
        service.createQR(user.getUsername());
    }

    @DeleteMapping("/qr")
    @PreAuthorize(HAS_ROLE_PACIENTE)
    public void deleteQR(@AuthenticationPrincipal final UserFront user) {
        service.deleteQR(user.getUsername());
    }


}
