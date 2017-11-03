package ar.com.utn.proyecto.qremergencias.ws.controller;

import ar.com.utn.proyecto.qremergencias.core.domain.UserFront;
import ar.com.utn.proyecto.qremergencias.ws.service.TempCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tempCode")
public class TempCodeController {

    private final TempCodeService tempCodeService;

    @Autowired
    public TempCodeController(final TempCodeService tempCodeService) {
        this.tempCodeService = tempCodeService;
    }

    @GetMapping("/verify/{tempCode}")
    @PreAuthorize("hasRole('MEDICO')")
    public String verifyTempCode(@PathVariable final String tempCode,
                                 @AuthenticationPrincipal final UserFront user) {
        return tempCodeService.verifyTempCode(tempCode, user);
    }

}
