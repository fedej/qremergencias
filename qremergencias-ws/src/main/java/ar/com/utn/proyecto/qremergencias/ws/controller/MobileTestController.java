package ar.com.utn.proyecto.qremergencias.ws.controller;

import ar.com.utn.proyecto.qremergencias.core.domain.UserFront;
import ar.com.utn.proyecto.qremergencias.core.dto.PublicKeyDTO;
import ar.com.utn.proyecto.qremergencias.core.dto.VerificationDTO;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/mobile")
public class MobileTestController {

    private final Map<String, byte[]> cache = new HashMap<>();

    @PutMapping("/upload")
    @PreAuthorize("hasRole('PACIENTE')")
    @ResponseStatus(HttpStatus.OK)
    public void uploadPublicKey(@RequestBody final PublicKeyDTO body,
                                @AuthenticationPrincipal
                                final UserFront user)
            throws UnsupportedEncodingException {
        cache.put(user.getId(), Base64.getDecoder().decode(body.getPublicKey()));
    }

    @PostMapping("/verify")
    @PreAuthorize("hasRole('MEDICO')")
    public boolean verifySignature(@RequestBody final VerificationDTO body)
            throws Exception {
        return verifySignature(body.getMessage(), Base64.getDecoder().decode(body.getSignature()));
    }

    private boolean verifySignature(String data, byte[] signature) throws Exception {
        final Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initVerify(getPublic(data));
        sig.update(data.getBytes());
        return sig.verify(signature);
    }

    private PublicKey getPublic(String key) throws Exception {
        final byte[] keyBytes = cache.get(key);
        final X509EncodedKeySpec spec =
                new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }

}
