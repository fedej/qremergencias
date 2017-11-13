package ar.com.utn.proyecto.qremergencias.ws.controller;

import ar.com.utn.proyecto.qremergencias.core.domain.UserFront;
import ar.com.utn.proyecto.qremergencias.core.dto.LoginUserDTO;
import ar.com.utn.proyecto.qremergencias.core.dto.MedicalRecordDTO;
import ar.com.utn.proyecto.qremergencias.core.dto.PublicKeyDTO;
import ar.com.utn.proyecto.qremergencias.core.dto.UserProfileDTO;
import ar.com.utn.proyecto.qremergencias.core.dto.VerificationDTO;
import ar.com.utn.proyecto.qremergencias.core.dto.emergency.EmergencyDataDTO;
import ar.com.utn.proyecto.qremergencias.ws.exceptions.PequeniaLisaException;
import ar.com.utn.proyecto.qremergencias.ws.service.EmergencyDataService;
import ar.com.utn.proyecto.qremergencias.ws.service.TempCodeService;
import ar.com.utn.proyecto.qremergencias.ws.service.UserFrontService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Map;

import static ar.com.utn.proyecto.qremergencias.util.CryptoUtils.verifySignature;
import static ar.com.utn.proyecto.qremergencias.ws.controller.MedicalRecordController.HAS_ROLE_MEDICO;

@RestController
@RequestMapping("/api/mobile")
@SuppressWarnings("PMD.TooManyMethods")
public class MobileRestController {

    private static final String HAS_ROLE_PACIENTE = "hasRole('PACIENTE')";
    private static final String IS_FULLY_AUTHENTICATED = "isFullyAuthenticated()";

    private final EmergencyDataController emergencyDataController;
    private final ProfileController profileController;
    private final MedicalRecordController medicalRecordController;
    private final EmergencyDataService emergencyDataService;
    private final TempCodeService tempCodeService;
    private final CacheManager cacheManager;
    private final UserFrontService userFrontService;
    private final UserFrontController userFrontController;

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
                                final MedicalRecordController medicalRecordController,
                                final EmergencyDataService emergencyDataService,
                                final TempCodeService tempCodeService,
                                final CacheManager cacheManager,
                                final UserFrontService userFrontService,
                                final UserFrontController userFrontController) {
        this.emergencyDataController = emergencyDataController;
        this.profileController = profileController;
        this.medicalRecordController = medicalRecordController;
        this.emergencyDataService = emergencyDataService;
        this.tempCodeService = tempCodeService;
        this.cacheManager = cacheManager;
        this.userFrontService = userFrontService;
        this.userFrontController = userFrontController;
    }

    @GetMapping("/emergencyData/{uuid}")
    public String getEmergencyDataByUuid(@PathVariable final String uuid,
                                         @RequestHeader(HttpHeaders.AUTHORIZATION) final String authorization)
            throws PequeniaLisaException {
        return emergencyDataController.getEmergencyDataByUuid(uuid);
    }

    @PatchMapping("/emergencyData")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('MEDICO')")
    public void updateEmergencyData(@Valid @RequestBody final EmergencyDataDTO emergencyDataDTO,
                                    @RequestParam final String userId,
                                    @RequestParam final boolean qrUpdateRequired) {
        emergencyDataController.updateEmergencyData(emergencyDataDTO, userId, qrUpdateRequired);
    }

    @GetMapping("/emergencyData")
    @PreAuthorize(HAS_ROLE_PACIENTE)
    public EmergencyDataDTO getEmergencyData(@AuthenticationPrincipal final UserFront userFront) {
        return emergencyDataController.getEmergencyData(userFront.getUsername());
    }

    @GetMapping("/emergencyData/medic")
    @PreAuthorize("isFullyAuthenticated()")
    public EmergencyDataDTO getUserEmergencyData(@RequestParam final String userId) {
        return emergencyDataController.getEmergencyData(userId);
    }

    @GetMapping("/tempCode/pk")
    @PreAuthorize("hasRole('MEDICO')")
    @ResponseStatus(HttpStatus.OK)
    public VerificationDTO verifyQRSignature(final String qrContent) throws UnsupportedEncodingException {

        final Instant timestamp = getTimestamp(qrContent);
        if (timestamp.isBefore(Instant.now().plus(3, ChronoUnit.MINUTES))) {
            final String username = getUser(qrContent);
            if (verifySignature(publicKeyCache.get(username, String.class), qrContent)) {
                return emergencyDataService.findByUser(username)
                        .map(emergencyData -> new VerificationDTO(emergencyData.getUser().getUsername(), null))
                        .orElseGet(() -> new VerificationDTO(null, "Datos no cargados aun"));
            } else {
                return new VerificationDTO(null, "Firma invalida");
            }
        } else {
            return new VerificationDTO(null, "QR Expirado");
        }

    }

    @GetMapping("/profile")
    @PreAuthorize(IS_FULLY_AUTHENTICATED)
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public UserProfileDTO getProfile(@AuthenticationPrincipal final UserFront userFront) {
        return profileController.list(userFront);
    }

    @PatchMapping("/profile")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize(IS_FULLY_AUTHENTICATED)
    public void updateProfile(@RequestBody final UserProfileDTO userProfileDTO,
                              @AuthenticationPrincipal final UserFront user,
                              @RequestParam final boolean qrUpdateRequired) {
        profileController.update(userProfileDTO, user, qrUpdateRequired);
    }

    @GetMapping(value = "/qr", produces = MediaType.IMAGE_PNG_VALUE)
    @PreAuthorize(HAS_ROLE_PACIENTE)
    public Resource getQR(@RequestParam(name = "user") final String user, final HttpServletResponse response) {
        return emergencyDataController.getQR(user, response);
    }

    @PostMapping("/qr")
    @PreAuthorize(HAS_ROLE_PACIENTE)
    public void createQR(@AuthenticationPrincipal final UserFront user) {
        emergencyDataController.createQR(user);
    }

    @DeleteMapping("/qr")
    @PreAuthorize(HAS_ROLE_PACIENTE)
    public void deleteQR(@AuthenticationPrincipal final UserFront user) {
        emergencyDataController.deleteQR(user);
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

    @GetMapping("/medicalRecord")
    @PreAuthorize(HAS_ROLE_PACIENTE)
    public Page<MedicalRecordDTO> listMyMedicalRecords(@PageableDefault final Pageable page,
                                                       @AuthenticationPrincipal final UserFront user) {
        return medicalRecordController.listMyRecords(page, user);
    }

    @GetMapping("/medicalRecord/user")
    @PreAuthorize(HAS_ROLE_MEDICO)
    public Page<MedicalRecordDTO> listPatientRecords(@PageableDefault final Pageable page,
                                                     @RequestParam final String username) {
        return medicalRecordController.listPatientRecords(page, username);
    }

    @GetMapping("/medicalRecord/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize(IS_FULLY_AUTHENTICATED)
    public MedicalRecordDTO findMedicalRecordById(@PathVariable final String id) {
        return medicalRecordController.findById(id);
    }

    @GetMapping("/medicalRecord/file/{fileId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize(IS_FULLY_AUTHENTICATED)
    @ApiIgnore("URL is handled by the backend")
    public Resource findMedicalRecordFileById(@PathVariable final String fileId, final HttpServletResponse response) {
        return medicalRecordController.findFileById(fileId, response);
    }

    @PostMapping(value = "/medicalRecord", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(HAS_ROLE_MEDICO)
    public Map<String, String> createMedicalRecord(@Valid final MedicalRecordDTO medicalRecord,
                                      @RequestPart(required = false, name = "file") final MultipartFile file,
                                      @AuthenticationPrincipal final UserFront user) {
        return medicalRecordController.create(medicalRecord, file, user);
    }

    @PatchMapping("/medicalRecord/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize(HAS_ROLE_MEDICO)
    public void updateMedicalRecord(@PathVariable final String id,
                                    @Valid @RequestBody final MedicalRecordDTO medicalRecord,
                                    @AuthenticationPrincipal final UserFront user) {
        medicalRecordController.update(id, medicalRecord, user);
    }

    @DeleteMapping("/medicalRecord/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(HAS_ROLE_MEDICO)
    public void deleteMedicalRecord(@PathVariable final String id,
                                    @AuthenticationPrincipal final UserFront user) {
        medicalRecordController.delete(id, user);
    }

    @PutMapping("/tempCode/upload")
    @PreAuthorize(HAS_ROLE_PACIENTE)
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
    public LoginUserDTO getUserInfo(@AuthenticationPrincipal final UserFront user, final String token) {
        userFrontService.setUserToken(user, token);
        return new LoginUserDTO(user.getName(), user.getLastname(), user.getRoles(), user.getEmail());
    }

    @RequestMapping(value = "/sendForgotPassword", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void sendForgotPassword(final HttpServletRequest request,
                                   @RequestParam final String username, final Locale locale,
                                   @RequestHeader(HttpHeaders.AUTHORIZATION) final String authorization) {
        userFrontController.sendForgotPassword(request, "HACK", username, locale);
    }

}
