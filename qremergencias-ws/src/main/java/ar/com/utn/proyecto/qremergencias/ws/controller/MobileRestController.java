package ar.com.utn.proyecto.qremergencias.ws.controller;

import ar.com.utn.proyecto.qremergencias.core.domain.UserFront;
import ar.com.utn.proyecto.qremergencias.core.domain.emergency.EmergencyData;
import ar.com.utn.proyecto.qremergencias.core.dto.LoginUserDTO;
import ar.com.utn.proyecto.qremergencias.core.dto.PublicKeyDTO;
import ar.com.utn.proyecto.qremergencias.core.dto.VerificationDTO;
import ar.com.utn.proyecto.qremergencias.core.dto.emergency.EmergencyDataDTO;
import ar.com.utn.proyecto.qremergencias.ws.exceptions.InvalidQRException;
import ar.com.utn.proyecto.qremergencias.ws.exceptions.PequeniaLisaException;
import ar.com.utn.proyecto.qremergencias.ws.service.EmergencyDataService;
import ar.com.utn.proyecto.qremergencias.ws.service.TempCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.io.UnsupportedEncodingException;
import java.util.Optional;

@RestController
@RequestMapping("/api/mobile")
public class MobileRestController {

    private final EmergencyDataController emergencyDataController;
    private final EmergencyDataService emergencyDataService;
    private final TempCodeService tempCodeService;
    private final CacheManager cacheManager;

    @Value("${qremergencias.tempCode.publicKey.cache}")
    private String publicKeyCacheName;

    private Cache publicKeyCache;

    @PostConstruct
    public void init() {
        publicKeyCache = cacheManager.getCache(publicKeyCacheName);
    }

    @Autowired
    public MobileRestController(final EmergencyDataController emergencyDataController,
                                final EmergencyDataService emergencyDataService,
                                final TempCodeService tempCodeService,
                                final CacheManager cacheManager) {
        this.emergencyDataController = emergencyDataController;
        this.emergencyDataService = emergencyDataService;
        this.tempCodeService = tempCodeService;
        this.cacheManager = cacheManager;
    }

    @GetMapping("/emergencyData/{uuid}")
    public String getEmergencyDataByUuid(@PathVariable final String uuid) throws PequeniaLisaException {
        return emergencyDataController.getEmergencyDataByUuid(uuid);
    }

    @GetMapping("/emergencyData")
    public EmergencyDataDTO getEmergencyData() {
        return new EmergencyDataDTO();
    }

    @GetMapping("/tempCode/pk")
    @PreAuthorize("hasRole('MEDICO')")
    @ResponseStatus(HttpStatus.OK)
    public VerificationDTO getPublicKey(final String user) throws UnsupportedEncodingException {
        final Optional<EmergencyData> emergencyData = emergencyDataService.findByUser(user);
        if (emergencyData.isPresent()) {
            return new VerificationDTO(emergencyData.get().getUuid(), publicKeyCache.get(user, String.class));
        }
        throw new InvalidQRException();
    }

    @PutMapping("/tempCode/upload")
    @PreAuthorize("hasRole('PACIENTE')")
    @ResponseStatus(HttpStatus.OK)
    public void uploadPublicKey(@RequestBody final PublicKeyDTO body,
                                @AuthenticationPrincipal
                                final UserFront user)
            throws UnsupportedEncodingException {
        publicKeyCache.put(user.getUsername(), body.getPublicKey());
    }

    @PutMapping("/tempCode/{uuid}")
    @PreAuthorize("hasRole('MEDICO')")
    public Integer createTempCode(@PathVariable final String uuid,
                                  @AuthenticationPrincipal final UserFront user) {
        return tempCodeService.createTempCode(uuid, user);
    }

    @GetMapping("/user/me")
    @PreAuthorize("hasAnyRole('MEDICO', 'PACIENTE')")
    public LoginUserDTO getUserInfo(@AuthenticationPrincipal final UserFront user) {
        return new LoginUserDTO(user.getName(), user.getLastname(), user.getRoles(), user.getEmail());
    }

}
