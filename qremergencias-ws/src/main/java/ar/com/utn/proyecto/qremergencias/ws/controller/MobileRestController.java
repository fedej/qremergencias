package ar.com.utn.proyecto.qremergencias.ws.controller;

import ar.com.utn.proyecto.qremergencias.core.domain.UserFront;
import ar.com.utn.proyecto.qremergencias.core.dto.LoginUserDTO;
import ar.com.utn.proyecto.qremergencias.core.dto.PublicKeyDTO;
import ar.com.utn.proyecto.qremergencias.core.dto.UserProfileDTO;
import ar.com.utn.proyecto.qremergencias.core.dto.VerificationDTO;
import ar.com.utn.proyecto.qremergencias.core.dto.emergency.EmergencyDataDTO;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static ar.com.utn.proyecto.qremergencias.util.CryptoUtils.verifySignature;

@RestController
@RequestMapping("/api/mobile")
public class MobileRestController {

    private final EmergencyDataController emergencyDataController;
    private final ProfileController profileController;
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
                                final ProfileController profileController,
                                final EmergencyDataService emergencyDataService,
                                final TempCodeService tempCodeService,
                                final CacheManager cacheManager) {
        this.emergencyDataController = emergencyDataController;
        this.profileController = profileController;
        this.emergencyDataService = emergencyDataService;
        this.tempCodeService = tempCodeService;
        this.cacheManager = cacheManager;
    }

    @GetMapping("/emergencyData/{uuid}")
    public String getEmergencyDataByUuid(@PathVariable final String uuid) throws PequeniaLisaException {
        return emergencyDataController.getEmergencyDataByUuid(uuid);
    }

    @GetMapping("/emergencyData")
    @PreAuthorize("hasRole('PACIENTE')")
    public EmergencyDataDTO getEmergencyData(@AuthenticationPrincipal final UserFront userFront) {
        return emergencyDataController.getEmergencyData(userFront.getUsername());
    }

    @GetMapping("/tempCode/pk")
    @PreAuthorize("hasRole('MEDICO')")
    @ResponseStatus(HttpStatus.OK)
    public VerificationDTO verifyQRSignature(final String user) throws UnsupportedEncodingException {

        final Instant timestamp = getTimestamp(user);
        if (timestamp.isBefore(Instant.now().plus(3, ChronoUnit.HOURS))) {
            final String username = getUser(user);
            if (verifySignature(publicKeyCache.get(username, String.class), user)) {
                return emergencyDataService.findByUser(username)
                        .map(emergencyData1 -> new VerificationDTO(emergencyData1.getUuid(), null))
                        .orElseGet(() -> new VerificationDTO(null, "Datos insuficientes"));
            } else {
                return new VerificationDTO(null, "Firma invalida");
            }
        } else {
            return new VerificationDTO(null, "QR Expirado");
        }

    }

    @GetMapping("/profile")
    @PreAuthorize("isFullyAuthenticated()")
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public UserProfileDTO getProfile(@AuthenticationPrincipal final UserFront userFront) {
        return profileController.list(userFront);
    }

    @PatchMapping("/profile")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isFullyAuthenticated()")
    public void updateProfile(@RequestBody final UserProfileDTO userProfileDTO,
                       @AuthenticationPrincipal final UserFront user,
                       @RequestParam final boolean qrUpdateRequired) {
        profileController.update(userProfileDTO, user, qrUpdateRequired);
    }

    private Instant getTimestamp(final String qr) {
        final Long timestamp = Long.valueOf(getTimestampAndUser(qr)[1]);
        return Instant.ofEpochMilli(timestamp);
    }

    private String getUser(final String qr) {
        return getTimestampAndUser(qr)[0];
    }

    private String[] getTimestampAndUser(final String qr) {
        final Integer signatureSize = Integer.valueOf(qr.substring(0, 3));
        return qr.substring(3 + signatureSize, qr.length()).split(" ");
    }

    @PutMapping("/tempCode/upload")
    @PreAuthorize("hasRole('PACIENTE')")
    @ResponseStatus(HttpStatus.OK)
    public void uploadPublicKey(@RequestBody final PublicKeyDTO body,
                                @AuthenticationPrincipal final UserFront user)
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
