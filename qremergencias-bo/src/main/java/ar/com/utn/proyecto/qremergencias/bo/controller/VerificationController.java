package ar.com.utn.proyecto.qremergencias.bo.controller;

import ar.com.utn.proyecto.qremergencias.bo.service.VerificationService;
import ar.com.utn.proyecto.qremergencias.core.domain.UserFront;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;

@Controller
@RequestMapping("/verification")
@PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
public class VerificationController {

    private static final String USER_INDEX = "verification/index";

    private final VerificationService verificationService;

    @Autowired
    public VerificationController(final VerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @GetMapping("/index")
    public String list(final Model model, final Pageable page) {
        final Page<UserFront> users = verificationService.findMedicos(page);
        model.addAttribute("page", users);
        model.addAttribute("rolesList", Arrays.asList("ROLE_ADMIN", "ROLE_OPERATOR"));
        return USER_INDEX;
    }

    @PostMapping("/verify")
    public String verify(@RequestParam final String id, final Model model, final Pageable page) {
        verificationService.verify(id);
        final Page<UserFront> users = verificationService.findMedicos(page);
        model.addAttribute("page", users);
        model.addAttribute("rolesList", Arrays.asList("ROLE_ADMIN", "ROLE_OPERATOR"));
        return USER_INDEX;
    }

    @PostMapping("/unverify")
    public String unverify(@RequestParam final String id, final Model model, final Pageable page) {
        verificationService.unverify(id);
        final Page<UserFront> users = verificationService.findMedicos(page);
        model.addAttribute("page", users);
        model.addAttribute("rolesList", Arrays.asList("ROLE_ADMIN", "ROLE_OPERATOR"));
        return USER_INDEX;
    }

}
