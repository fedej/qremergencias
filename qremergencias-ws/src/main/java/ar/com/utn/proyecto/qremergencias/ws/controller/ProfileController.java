package ar.com.utn.proyecto.qremergencias.ws.controller;

import ar.com.utn.proyecto.qremergencias.core.domain.UserFront;
import ar.com.utn.proyecto.qremergencias.core.dto.UserContactDTO;
import ar.com.utn.proyecto.qremergencias.core.dto.UserProfileDTO;
import ar.com.utn.proyecto.qremergencias.core.repository.UserFrontRepository;
import ar.com.utn.proyecto.qremergencias.ws.service.UserProfileService;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.session.ExpiringSession;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;

import static ar.com.utn.proyecto.qremergencias.core.mapper.Converters.listConverter;
import static org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;

@RestController
@RequestMapping("/api/profile")
@Log
public class ProfileController {

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private UserFrontRepository userFrontRepository;

    @Autowired
    private SessionRepository<? extends ExpiringSession> sessionRepository;

    @GetMapping
    @PreAuthorize("isFullyAuthenticated()")
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public UserProfileDTO list(@AuthenticationPrincipal final UserFront loggedInUser) {
        log.info("In ProfileController.list()");
        final UserFront userFront = userFrontRepository.findByUsername(loggedInUser.getUsername());
        final UserProfileDTO userProfileDTO = new UserProfileDTO();
        userProfileDTO.setFirstName(userFront.getName());
        userProfileDTO.setLastName(userFront.getLastname());
        userProfileDTO.setBirthDate(userFront.getBirthdate());
        userProfileDTO.setIdNumber(userFront.getIdNumber());
        userProfileDTO.setSex(userFront.getSex());
        final List<UserContactDTO> contacts = listConverter(UserContactDTO.USER_CONTACT_DTO_MAPPER)
                .apply(userFront.getContacts());
        userProfileDTO.setContacts(contacts);
        return userProfileDTO;
    }

    private void updateSession(final UserFront user) {
        final String sessionId = RequestContextHolder.currentRequestAttributes().getSessionId();
        final ExpiringSession session = sessionRepository.getSession(sessionId);

        if (session != null) {
            final SecurityContext context = session.getAttribute(SPRING_SECURITY_CONTEXT_KEY);
            final Authentication auth = context.getAuthentication();
            context.setAuthentication(new UsernamePasswordAuthenticationToken(user,
                    auth.getCredentials(), auth.getAuthorities()));
            session.setAttribute(SPRING_SECURITY_CONTEXT_KEY, context);

            try {
                final MethodHandle saver = MethodHandles.lookup()
                        .findVirtual(sessionRepository.getClass(), "save",
                                MethodType.methodType(void.class, Session.class));
                saver.invoke(sessionRepository, session);
            } catch (final Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        }

    }

    @PatchMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isFullyAuthenticated()")
    public void update(@RequestBody final UserProfileDTO userProfileDTO,
                       @AuthenticationPrincipal final UserFront user,
                       @RequestParam final boolean qrUpdateRequired) {
        log.info("In ProfileController.update()");
        final UserFront updated = userProfileService.update(user, userProfileDTO, qrUpdateRequired);
        updateSession(updated);
    }

}
