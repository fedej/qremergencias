package ar.com.utn.proyecto.qremergencias.ws.controller;

import ar.com.utn.proyecto.qremergencias.core.domain.User;
import ar.com.utn.proyecto.qremergencias.core.domain.UserFront;
import ar.com.utn.proyecto.qremergencias.core.domain.UserVerificationToken;
import ar.com.utn.proyecto.qremergencias.core.dto.ChangePasswordDTO;
import ar.com.utn.proyecto.qremergencias.core.dto.CreateUserDTO;
import ar.com.utn.proyecto.qremergencias.core.dto.LoginUserDTO;
import ar.com.utn.proyecto.qremergencias.core.dto.ResetPasswordDTO;
import ar.com.utn.proyecto.qremergencias.core.service.ForgotPasswordService;
import ar.com.utn.proyecto.qremergencias.core.service.MailService;
import ar.com.utn.proyecto.qremergencias.core.service.PasswordChangeService;
import ar.com.utn.proyecto.qremergencias.core.service.UserService;
import ar.com.utn.proyecto.qremergencias.ws.auth.LoginAdapter;
import ar.com.utn.proyecto.qremergencias.ws.service.UserFrontService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;
import org.thymeleaf.context.Context;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Locale;

@RestController
@RequestMapping("/api/userFront")
public class UserFrontController {

    //private static final String SUBJECT = "default.forgot.email.subject";
    private static final String GREETING_SUBJECT = "default.greeting.email.subject";
    private static final String INVALID_PASSWORD = "Invalid password";
    private static final String INVALID_TOKEN = "Invalid token";
    private static final String TOKEN_NOT_FOUND = "Token not found";

    //@Value("${qremergencias.front.baseUrl}")
    //private String baseUrl;

    //@Value("${qremergencias.front.resetPasswordUrl}")
    //private String resetPasswordUrl;

    @Value("${qremergencias.front.confirmRegistrationUrl}")
    private String confirmRegistrationUrl;

    @Autowired
    private UserFrontService userFrontService;

    @Autowired
    private ForgotPasswordService forgotPasswordService;

    //@Autowired
    //private CaptchaService captchaService;

    @Autowired
    private MailService mailService;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private PasswordChangeService passwordChangeService;

    @Autowired
    private LoginAdapter loginAdapter;

    @Autowired
    private UserService userService;

    @RequestMapping(value = "/register", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public LoginUserDTO register(@Valid @RequestBody final CreateUserDTO model,
                                 final NativeWebRequest request) {


        final UserFront user = userFrontService.create(model);
        if (user != null) {
            //sendGreetingMail(user);
            sendMailConfirmation(user.getUsername());
            return loginAdapter.login(user, model.getPassword(), request);
        }
        return null;
    }

    @SuppressWarnings("PMD.DUAnomaly")
    @RequestMapping(value = "/sendMailConfirmation", method = RequestMethod.POST)
    public void sendMailConfirmation(@RequestParam final String username) {

        final UserFront userFront = userFrontService.findByUsername(username);

        final User user = userService.findByUsername(username);

        if (!StringUtils.isEmpty(userFront.getEmail())) {
            final Locale locale = LocaleContextHolder.getLocale();
            final Context ctx = new Context(locale);
            ctx.setVariable("username", userFront.getUsername());
            ctx.setVariable("url",
                    confirmRegistrationUrl
                            + userService.getUserVerificationByUser(user).getToken());

            final Resource header = resourceLoader
                    .getResource("classpath:static/images/mail/header-mail.jpg");

            final Resource button = resourceLoader
                    .getResource("classpath:static/images/mail/btn-codigo.png");

            mailService.sendMail(user.getEmail(),
                    messageSource.getMessage(GREETING_SUBJECT, null, locale), "mail/greeting", ctx,
                    Arrays.asList(header, button));
        }

    }

    @RequestMapping(value = "/resetPassword", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void resetPassword(@Valid final ResetPasswordDTO resetPassword) {

        if (!resetPassword.getNewPassword().equals(resetPassword.getConfirmPassword())) {
            throw new RuntimeException(INVALID_PASSWORD);
        }

        if (!forgotPasswordService.validate(resetPassword.getToken(),
                resetPassword.getNewPassword())) {
            throw new RuntimeException(INVALID_PASSWORD);
        }

        forgotPasswordService.changePassword(resetPassword.getToken(),
                resetPassword.getNewPassword());

    }

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/changePassword", method = RequestMethod.POST)
    public void changePassword(@Valid final ChangePasswordDTO changePassword,
                               final BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new RuntimeException(INVALID_PASSWORD);
        }

        final Authentication authentication = SecurityContextHolder
                .getContext().getAuthentication();
        final User user = (User) authentication.getPrincipal();

        final boolean validPassword = passwordChangeService.validate(changePassword.getPassword(),
                changePassword.getNewPassword(), user);

        if (!validPassword) {
            throw new RuntimeException(INVALID_PASSWORD);
        }

        passwordChangeService.changePassword(user, changePassword.getNewPassword());
    }

    /*private void sendGreetingMail(final UserFront user) {
        final Locale locale = LocaleContextHolder.getLocale();
        final Context ctx = new Context(locale);

        ctx.setVariable("url", baseUrl);

        final Resource header = resourceLoader
                .getResource("classpath:static/images/mail/header-mail.jpg");

        final Resource button = resourceLoader
                .getResource("classpath:static/images/mail/btn-codigo.png");

        mailService.sendMail(user.getEmail(),
                messageSource.getMessage(GREETING_SUBJECT, null, locale), "mail/greeting", ctx,
                Arrays.asList(header, button));
    }
*/
    @SuppressWarnings("PMD.DUAnomaly")
    @RequestMapping(value = "/confirmRegistration", method = RequestMethod.GET)
    public void confirmRegistration(@RequestParam("token") final String token) {
        final UserVerificationToken userVerificationToken = userFrontService
                .getUserVerificationByToken(token);
        if (userVerificationToken == null) {
            throw new RuntimeException(TOKEN_NOT_FOUND);
        }
        final User user = userVerificationToken.getUser();

        if (userVerificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException(INVALID_TOKEN);
        }
        user.setEnabled(true);
        userFrontService.save(user);
    }

}
