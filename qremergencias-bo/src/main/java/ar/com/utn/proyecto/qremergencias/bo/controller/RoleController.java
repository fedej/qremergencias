package ar.com.utn.proyecto.qremergencias.bo.controller;

import ar.com.utn.proyecto.qremergencias.bo.service.FlashMessageService;
import ar.com.utn.proyecto.qremergencias.core.domain.Role;
import ar.com.utn.proyecto.qremergencias.core.service.RoleService;
import ar.com.utn.proyecto.qremergencias.core.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@RequestMapping("/role")
@PreAuthorize("hasRole('ADMIN')")
public class RoleController {

    private static final String ROLE_INDEX = "role/index";
    private static final String ROLE_CREATE = "role/create";
    private static final String ROLE_EDIT = "role/edit";
    private static final String ROLE_SHOW = "role/show";
    private static final String ROLE = "role";

    @Autowired
    private RoleService roleService;

    @Autowired
    private UserService userService;

    @Autowired
    private FlashMessageService flashMessageService;

    @RequestMapping(value = "/index", method = RequestMethod.GET)
    @ModelAttribute("page")
    public Page<Role> list(final Pageable page) {
        return roleService.listRoles(page);
    }

    @RequestMapping(value = "/create", method = RequestMethod.GET)
    public String create(final Model model) {
        model.addAttribute(ROLE, new Role());
        return ROLE_CREATE;
    }

    @ModelAttribute(ROLE)
    @RequestMapping(value = "/edit", method = RequestMethod.GET)
    public Role edit(final String id) {
        return roleService.findOne(id);
    }

    @RequestMapping(value = "/show", method = RequestMethod.GET)
    public String show(@RequestParam final String id, final Model model) {
        model.addAttribute(ROLE, roleService.findOne(id));
        return ROLE_SHOW;
    }


    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public String delete(final String id, final RedirectAttributes redirectAttributes) {

        roleService.delete(id);

        flashMessageService.addFlashMessage(redirectAttributes, "role.delete.success");

        return "redirect:/" + ROLE_INDEX;
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public String update(@Valid final Role role, final BindingResult bindingResult,
                         final Model model, @RequestParam final String adminpassword,
                         final RedirectAttributes redirectAttributes) {

        if (userService.isNotAdmin(adminpassword)) {
            bindingResult.addError(new ObjectError("adminpassword",
                    new String[]{"validation.invalid.password"}, null, null));
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute(ROLE, role);
            return ROLE_EDIT;
        }

        final Role roleUpdated = roleService.update(role);

        model.addAttribute(ROLE, roleUpdated);

        flashMessageService.addFlashMessage(redirectAttributes, "role.update.flash.message");

        return ROLE_SHOW;

    }

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public String save(@Valid final Role role, final BindingResult bindingResult,
                       final Model model, @RequestParam final String adminpassword,
                       final RedirectAttributes redirectAttributes) {

        if (userService.isNotAdmin(adminpassword)) {
            bindingResult.addError(new ObjectError("adminpassword",
                    new String[]{"validation.invalid.password"}, null, null));
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute(ROLE, role);
            return ROLE_CREATE;
        }

        final Role roleCreated = roleService.save(role);
        model.addAttribute(ROLE, roleCreated);

        flashMessageService.addFlashMessage(redirectAttributes, "role.create.flash.message");

        return ROLE_SHOW;
    }

}
