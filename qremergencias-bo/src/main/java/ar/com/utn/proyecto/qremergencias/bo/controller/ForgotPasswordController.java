package ar.com.utn.proyecto.qremergencias.bo.controller;

import ar.com.utn.proyecto.qremergencias.core.config.ApiLoginConfigurer;
import ar.com.utn.proyecto.qremergencias.core.domain.ForgotPassword;
import ar.com.utn.proyecto.qremergencias.core.domain.Role;
import ar.com.utn.proyecto.qremergencias.core.domain.User;
import ar.com.utn.proyecto.qremergencias.bo.dto.ExpiredPasswordDTO;
import ar.com.utn.proyecto.qremergencias.bo.dto.ForgotPasswordDTO;
import ar.com.utn.proyecto.qremergencias.core.dto.ResetPasswordDTO;
import ar.com.utn.proyecto.qremergencias.core.repository.UserRepository;
import ar.com.utn.proyecto.qremergencias.bo.service.FlashMessageService;
import ar.com.utn.proyecto.qremergencias.core.service.ForgotPasswordService;
import ar.com.utn.proyecto.qremergencias.core.service.MailService;
import ar.com.utn.proyecto.qremergencias.bo.service.PasswordExpiredService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.thymeleaf.context.Context;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.Collections;
import java.util.Locale;

@Controller
@RequestMapping("/forgotPassword")
public class ForgotPasswordController {

    private static final String SUBJECT = "default.forgot.email.subject";
    private static final String ERROR = "redirect:/error";
    private static final String INDEX = "forgotPassword/index";
    private static final String RESET_PASSWORD = "forgotPassword/resetPassword";
    private static final String CREDENTIALS_EXPIRED = "forgotPassword/credentialsExpired";
    private static final String PASSWORD_CHANGED = "forgotPassword/passwordChanged";
    private static final String FORGOT_PASSWORD_DTO = "forgotPasswordDTO";
    private static final String FORGOT_PASSWORD_EMAIL_ERROR = "forgotPassword.email.error";
    private static final String FORGOT_PASSWORD_EMAIL_SENT = "forgotPassword.email.sent";
    private static final String EXPIRED_PASSWORD = "expiredPassword";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ForgotPasswordService forgotPasswordService;

    @Autowired
    private PasswordExpiredService passwordExpiredService;

    @Autowired
    private MailService mailService;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private FlashMessageService flashMessageService;

    @Autowired
    private ResourceLoader resourceLoader;

    @Value("${qremergencias.baseUrl}")
    private String baseUrl;

    @RequestMapping("/index")
    @ModelAttribute(FORGOT_PASSWORD_DTO)
    public ForgotPasswordDTO index() {
        return new ForgotPasswordDTO();
    }

    @RequestMapping(value = "/sendMailConfirmation", method = RequestMethod.POST)
    public String sendMailConfirmation(@Valid final ForgotPasswordDTO forgotPasswordDTO,
                        final BindingResult bindingResult, final Locale locale, final Model model) {

        if (bindingResult.hasErrors()) {
            flashMessageService.addFlashError(model, FORGOT_PASSWORD_EMAIL_ERROR);
            model.addAttribute(FORGOT_PASSWORD_DTO, new ForgotPasswordDTO());
            return INDEX;
        }

        final User dbUser = userRepository.findByUsernameAndEmail(forgotPasswordDTO.getUsername(),
                forgotPasswordDTO.getEmail());

        final Role role = new Role();
        role.setAuthority(Role.ROLE_USER);

        if (dbUser == null || dbUser.getRoles().contains(role)) {
            flashMessageService.addFlashMessage(model, FORGOT_PASSWORD_EMAIL_SENT);
            model.addAttribute(FORGOT_PASSWORD_DTO, new ForgotPasswordDTO());
            return INDEX;
        }

        final ForgotPassword forgotPassword = forgotPasswordService.create(dbUser);

        final Context ctx = new Context(locale);
        ctx.setVariable("username", dbUser.getUsername());
        ctx.setVariable("url", baseUrl + "/forgotPassword/resetPassword/"
                                        + forgotPassword.getToken());

        ctx.setVariable("baseUrl", baseUrl);

        mailService.sendMail(dbUser.getEmail(), messageSource.getMessage(SUBJECT, null, locale),
                "forgotPassword", ctx,
                Collections.singletonList(resourceLoader
                        .getResource("classpath:static/images/mail/logo-footer.png")));

        flashMessageService.addFlashMessage(model, FORGOT_PASSWORD_EMAIL_SENT);
        model.addAttribute(FORGOT_PASSWORD_DTO, new ForgotPasswordDTO());

        return INDEX;
    }

    @RequestMapping(value = "/resetPassword/{token}", method = RequestMethod.GET)
    public String resetPassword(@PathVariable final String token, final Model model) {

        if (forgotPasswordService.validate(token, null)) {
            final ResetPasswordDTO resetPassword = new ResetPasswordDTO();
            resetPassword.setToken(token);
            model.addAttribute("resetPassword", resetPassword);
            return RESET_PASSWORD;
        } else {
            return ERROR;
        }

    }

    @RequestMapping(value = "/resetPassword", method = RequestMethod.POST)
    public String resetPassword(@Valid final ResetPasswordDTO resetPassword, final Model model) {

        if (!resetPassword.getNewPassword().equals(resetPassword.getConfirmPassword())) {
            flashMessageService.addFlashError(model, "resetPassword.noMatch");
            model.addAttribute("resetPassword", resetPassword);
            return RESET_PASSWORD;
        }

        if (!forgotPasswordService.validate(resetPassword.getToken(),
                resetPassword.getNewPassword())) {
            flashMessageService.addFlashError(model, "resetPassword.invalid.password");
            model.addAttribute("resetPassword", resetPassword);
            return RESET_PASSWORD;
        }

        forgotPasswordService.changePassword(resetPassword.getToken(),
                resetPassword.getNewPassword());

        model.addAttribute("siteUrl", baseUrl);

        return PASSWORD_CHANGED;

    }

    /**
     * Stores the username of the user which has expired credentials.
     *
     * @param session
     *          session where the username must be stored.
     *
     * @param model
     *          form model.
     * @return
     *          where to redirect the user after the request is completed.
     */
    @RequestMapping(value = "/credentialsExpired", method = RequestMethod.GET)
    public String passwordExpired(final HttpSession session, final Model model) {

        final String username = (String) session
                .getAttribute(ApiLoginConfigurer.USERNAME_PARAMETER);

        if (StringUtils.isEmpty(username)) {
            return ERROR;
        }

        model.addAttribute(EXPIRED_PASSWORD, new ExpiredPasswordDTO());
        return CREDENTIALS_EXPIRED;
    }

    /**
     * Validate and change an expired password.
     *
     * @param expiredPassword
     *                  expired password form model.
     * @param session
     *                  session where the username is stored.
     * @param model
     *                  form model.
     * @return
     *                  where to redirect the user after the request is completed.
     */
    @RequestMapping(value = "/credentialsExpired", method = RequestMethod.POST)
    public String passwordExpired(@Valid final ExpiredPasswordDTO expiredPassword,
                                  final HttpSession session,
                                  final Model model) {

        final String username = (String) session
                .getAttribute(ApiLoginConfigurer.USERNAME_PARAMETER);

        if (StringUtils.isEmpty(username)) {
            return ERROR;
        }

        if (!expiredPassword.getNewPassword().equals(expiredPassword.getConfirmPassword())) {
            flashMessageService.addFlashError(model, "resetPassword.noMatch");
            model.addAttribute(EXPIRED_PASSWORD, new ExpiredPasswordDTO());
            return CREDENTIALS_EXPIRED;
        }

        if (!expiredPassword.getNewPassword().equals(expiredPassword.getConfirmPassword())) {
            flashMessageService.addFlashError(model, "resetPassword.noMatch");
            model.addAttribute(EXPIRED_PASSWORD, new ExpiredPasswordDTO());
            return CREDENTIALS_EXPIRED;
        }

        if (!passwordExpiredService.validate(username, expiredPassword.getPassword(),
                expiredPassword.getNewPassword())) {
            flashMessageService.addFlashError(model, "resetPassword.invalid.password");
            model.addAttribute(EXPIRED_PASSWORD, new ExpiredPasswordDTO());
            return CREDENTIALS_EXPIRED;
        }

        passwordExpiredService.changePassword(username, expiredPassword.getNewPassword());

        model.addAttribute("siteUrl", baseUrl);

        return PASSWORD_CHANGED;
    }


}
