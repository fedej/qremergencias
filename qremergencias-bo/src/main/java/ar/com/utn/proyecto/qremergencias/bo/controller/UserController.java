package ar.com.utn.proyecto.qremergencias.bo.controller;

import ar.com.utn.proyecto.qremergencias.bo.dto.EditUserDTO;
import ar.com.utn.proyecto.qremergencias.bo.service.FlashMessageService;
import ar.com.utn.proyecto.qremergencias.core.domain.User;
import ar.com.utn.proyecto.qremergencias.core.dto.ChangePasswordDTO;
import ar.com.utn.proyecto.qremergencias.core.service.PasswordChangeService;
import ar.com.utn.proyecto.qremergencias.core.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.Arrays;

@Controller
@RequestMapping("/user")
@PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
public class UserController {

    private static final String USER_INDEX = "user/index";
    private static final String USER_CREATE = "user/create";
    private static final String USER_EDIT = "user/edit";
    private static final String USER_SHOW = "user/show";
    private static final String USER_CHANGE_PASSWORD = "user/changePassword";
    private static final String USER = "user";

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordChangeService passwordChangeService;

    @Autowired
    private FlashMessageService flashMessageService;

    @RequestMapping(value = "/index", method = RequestMethod.GET)
    @SuppressWarnings("PMD.ConfusingTernary")
    public String list(final String role, final Model model, final Pageable page) {

        Page<User> users;

        if (role != null && !"".equals(role)) {
            users = userService.findByRole(role, page);
        } else {
            users = userService.findAll(page);
        }

        model.addAttribute("page", users);
        model.addAttribute("rolesList", Arrays.asList("ROLE_ADMIN", "ROLE_OPERATOR"));
        return USER_INDEX;
    }

    @RequestMapping(value = "/changePassword", method = RequestMethod.GET)
    public String changePassword(final Model model, @AuthenticationPrincipal final User user) {
        final ChangePasswordDTO value = new ChangePasswordDTO();
        value.setId(user.getId());
        model.addAttribute("changePassword", value);
        return USER_CHANGE_PASSWORD;
    }

    @RequestMapping(value = "/changePassword", method = RequestMethod.POST)
    public String changePassword(
            @Valid @ModelAttribute("changePassword") final ChangePasswordDTO changePassword,
            final BindingResult bindingResult, final Model model) {

        if (bindingResult.hasErrors()) {
            flashMessageService.addFlashError(model, "passwordChange.invalid.password");
            return USER_CHANGE_PASSWORD;
        }

        final User user = userService.findById(changePassword.getId());

        final boolean validPassword = passwordChangeService.validate(changePassword.getPassword(),
                changePassword.getNewPassword(), user);

        if (!validPassword) {
            flashMessageService.addFlashError(model, "passwordChange.invalid.password");
            return USER_CHANGE_PASSWORD;
        }

        passwordChangeService.changePassword(user.getUsername(), changePassword.getNewPassword());
        model.addAttribute(USER, user);
        return USER_SHOW;
    }

    @RequestMapping(value = "/create", method = RequestMethod.GET)
    public String create(final Model model) {
        model.addAttribute(USER, new User());
        return USER_CREATE;
    }

    @RequestMapping(value = "/show", method = RequestMethod.GET)
    public String show(@RequestParam final String id, final Model model) {
        model.addAttribute(USER, userService.findById(id));
        return USER_SHOW;
    }

    @ModelAttribute(USER)
    @RequestMapping(value = "/edit", method = RequestMethod.GET)
    public User edit(final String id) {
        return userService.findById(id);
    }

    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public String delete(final String id, final RedirectAttributes redirectAttributes,
            @AuthenticationPrincipal final User user) {
        final boolean deleted = userService.delete(id, user);

        if (deleted) {
            flashMessageService.addFlashMessage(redirectAttributes, "user.delete.success");
        } else {
            flashMessageService.addFlashError(redirectAttributes, "user.delete.error");
        }

        return "redirect:/" + USER_INDEX;
    }

    @RequestMapping(value = "/update", method = RequestMethod.PUT)
    public String update(@Valid @ModelAttribute final EditUserDTO user,
            final BindingResult bindingResult, final Model model,
            @RequestParam final String adminpassword, final RedirectAttributes redirectAttributes) {

        if (userService.isNotAdmin(adminpassword)) {
            bindingResult.addError(new ObjectError("adminpassword",
                    new String[] { "validation.invalid.password" }, null, null));
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute(USER, user);
            return USER_EDIT;
        }

        final User toUpdate = new User();
        toUpdate.setAccountNonLocked(user.isAccountNonLocked());
        toUpdate.setCredentialsNonExpired(user.isCredentialsNonExpired());
        toUpdate.setRoles(user.getRoles());
        toUpdate.setEnabled(user.isEnabled());
        toUpdate.setId(user.getId());

        final User updated = userService.update(toUpdate);

        if (updated == null) {
            flashMessageService.addFlashError(redirectAttributes, "user.update.error");
            return "redirect:" + USER_INDEX;
        }

        model.addAttribute(USER, updated);
        return USER_SHOW;
    }

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public String save(@Valid final User user, final BindingResult bindingResult, final Model model,
            @RequestParam final String adminpassword) {

        if (!user.getPassword().equals(user.getRepassword())) {
            bindingResult.addError(new FieldError(USER, "repassword", user.getRepassword(), false,
                    new String[] { "validation.invalid.repassword" }, null, null));
        }

        if (userService.isNotAdmin(adminpassword)) {
            bindingResult.addError(new ObjectError("adminpassword",
                    new String[] { "validation.invalid.password" }, null, null));
        }

        if (userService.exists(user.getUsername())) {
            bindingResult.addError(new FieldError(USER, "username", user.getUsername(), false,
                    new String[] { "validation.invalid.username" }, null, null));
        }

        if (bindingResult.hasErrors()) {
            return USER_CREATE;
        }

        user.getRoles().add("ROLE_OPERATOR");

        userService.save(user);

        model.addAttribute(USER, user);
        return USER_SHOW;
    }

}
