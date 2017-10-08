package ar.com.utn.proyecto.qremergencias.ws.controller;

import ar.com.utn.proyecto.qremergencias.core.domain.UserFront;
import ar.com.utn.proyecto.qremergencias.core.domain.emergency.EmergencyData;
import ar.com.utn.proyecto.qremergencias.core.dto.PublicKeyDTO;
import ar.com.utn.proyecto.qremergencias.core.dto.VerificationDTO;
import ar.com.utn.proyecto.qremergencias.ws.service.EmergencyDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.io.UnsupportedEncodingException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping("/api/mobile")
public class MobileTestController {

    private final CacheManager cacheManager;
    private final EmergencyDataService emergencyDataService;
    private final Random random = new Random(17923);

    private Cache publicKeyCache;
    private Cache tempCodeCache;

    @Value("${qremergencias.tempCode.publicKey.cache}")
    private String publicKeyCacheName;

    @Value("${qremergencias.tempCode.cache}")
    private String tempCodeCacheName;

    @Autowired
    public MobileTestController(final CacheManager cacheManager,
                                final EmergencyDataService emergencyDataService) {
        this.cacheManager = cacheManager;
        this.emergencyDataService = emergencyDataService;
    }

    @PostConstruct
    public void init() {
        publicKeyCache = cacheManager.getCache(publicKeyCacheName);
        tempCodeCache = cacheManager.getCache(tempCodeCacheName);
    }

    @PutMapping("/upload")
    @PreAuthorize("hasRole('PACIENTE')")
    @ResponseStatus(HttpStatus.OK)
    public void uploadPublicKey(@RequestBody final PublicKeyDTO body,
                                @AuthenticationPrincipal
                                final UserFront user)
            throws UnsupportedEncodingException {
        publicKeyCache.put(user.getId(), Base64.getDecoder().decode(body.getPublicKey()));
    }

    @PostMapping("/verify")
    @PreAuthorize("hasRole('MEDICO')")
    public String verifySignature(@RequestBody final VerificationDTO body)
            throws Exception {
        boolean verified = verifySignature(body.getMessage(), Base64.getDecoder().decode(body.getSignature()));
        return verified ?
                emergencyDataService.findByUuid(body.getMessage()).get().getUser().getUsername() :
                null;
    }

    @PutMapping("/tempCode/{uuid}")
    @PreAuthorize("hasRole('MEDICO')")
    public Integer createTempCode(@PathVariable final String uuid)
            throws Exception {
        Optional<EmergencyData> byUuid = emergencyDataService.findByUuid(uuid);
        final String username = byUuid.get().getUser().getUsername();
        final int tempCode = (int) (100000 + random.nextDouble() * 900000);
        tempCodeCache.put(tempCode, username);
        return tempCode;
    }

    @GetMapping("/tempCode/verify/{tempCode}")
    @PreAuthorize("hasRole('MEDICO')")
    public String verifyTempCode(@PathVariable final Integer tempCode)
            throws Exception {
        return tempCodeCache.get(tempCode, String.class);
    }

    private boolean verifySignature(String data, byte[] signature) throws Exception {
        final Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initVerify(getPublic(data));
        sig.update(data.getBytes());
        return sig.verify(signature);
    }

    private PublicKey getPublic(String key) throws Exception {
        final byte[] keyBytes = publicKeyCache.get(key, byte[].class);
        final X509EncodedKeySpec spec =
                new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }

}
