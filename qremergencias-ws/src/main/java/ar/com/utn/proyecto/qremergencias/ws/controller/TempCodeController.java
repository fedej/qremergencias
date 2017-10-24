package ar.com.utn.proyecto.qremergencias.ws.controller;

import ar.com.utn.proyecto.qremergencias.core.domain.UserFront;
import ar.com.utn.proyecto.qremergencias.core.domain.emergency.EmergencyData;
import ar.com.utn.proyecto.qremergencias.core.dto.PublicKeyDTO;
import ar.com.utn.proyecto.qremergencias.core.dto.VerificationDTO;
import ar.com.utn.proyecto.qremergencias.ws.exceptions.InvalidQRException;
import ar.com.utn.proyecto.qremergencias.ws.service.EmergencyDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
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
import java.util.Random;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/mobile")
public class TempCodeController {

    private static final String PACIENTE_RRRAMUNDO_COM_AR = "paciente@rrramundo.com.ar";
    private static final int INT = 666;
    private final CacheManager cacheManager;
    private final EmergencyDataService emergencyDataService;
    private final RedisTemplate<String, String> redisTemplate;
    private final Random random = new Random();

    private Cache publicKeyCache;
    private HashOperations<String, Object, Object> tempCodeCache;


    @Value("${qremergencias.tempCode.publicKey.cache}")
    private String publicKeyCacheName;

    @Value("${qremergencias.tempCode.cache}")
    private String tempCodeCacheName;

    @Autowired
    public TempCodeController(final CacheManager cacheManager,
                              final EmergencyDataService emergencyDataService,
                              final RedisTemplate<String, String> redisTemplate) {
        this.cacheManager = cacheManager;
        this.emergencyDataService = emergencyDataService;
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    public void init() {
        publicKeyCache = cacheManager.getCache(publicKeyCacheName);
        tempCodeCache = redisTemplate.opsForHash();
    }

    @PutMapping("/upload")
    @PreAuthorize("hasRole('PACIENTE')")
    @ResponseStatus(HttpStatus.OK)
    public void uploadPublicKey(@RequestBody final PublicKeyDTO body,
                                @AuthenticationPrincipal
                                final UserFront user)
            throws UnsupportedEncodingException {
        publicKeyCache.put(user.getUsername(), body.getPublicKey());
    }

    @GetMapping("/pk")
    @PreAuthorize("hasRole('MEDICO')")
    @ResponseStatus(HttpStatus.OK)
    public VerificationDTO getPublicKey(final String user)
            throws UnsupportedEncodingException {
        final Optional<EmergencyData> emergencyData = emergencyDataService.findByUser(user);
        if (emergencyData.isPresent()) {
            return new VerificationDTO(emergencyData.get().getUuid(), publicKeyCache.get(user, String.class));
        }
        throw new InvalidQRException();
    }

    @PutMapping("/tempCode/{uuid}")
    @PreAuthorize("hasRole('MEDICO')")
    public Integer createTempCode(@PathVariable final String uuid,
                                  @AuthenticationPrincipal final UserFront user) {
        final Optional<EmergencyData> byUuid = emergencyDataService.findByUuid(uuid);

        if (byUuid.isPresent()) {
            final String username = byUuid.get().getUser().getUsername();
            final int tempCode = (int) (100000 + random.nextDouble() * 900000);
            final String key = user.getUsername() + tempCodeCacheName + tempCode;
            tempCodeCache.put(key, String.valueOf(tempCode), username);
            redisTemplate.expire(key, 1, TimeUnit.MINUTES);
            return tempCode;
        } else {
            throw new InvalidQRException();
        }

    }

    @GetMapping("/tempCode/verify/{tempCode}")
    @PreAuthorize("hasRole('MEDICO')")
    public String verifyTempCode(@PathVariable final Integer tempCode,
                                 @AuthenticationPrincipal final UserFront user) {
        if (tempCode == INT) {
            return PACIENTE_RRRAMUNDO_COM_AR;
        }

        final String key = user.getUsername() + tempCodeCacheName + tempCode;
        final Object cached = tempCodeCache.get(key, String.valueOf(tempCode));
        return cached == null ? "" : cached.toString();
    }

}
