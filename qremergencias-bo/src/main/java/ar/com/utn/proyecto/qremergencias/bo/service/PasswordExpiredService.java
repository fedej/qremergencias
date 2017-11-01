package ar.com.utn.proyecto.qremergencias.bo.service;

import ar.com.utn.proyecto.qremergencias.core.domain.User;
import ar.com.utn.proyecto.qremergencias.core.service.PasswordChangeService;
import ar.com.utn.proyecto.qremergencias.core.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service to validate and change an expired password.
 */
@Service
public class PasswordExpiredService {

    @Autowired
    private PasswordChangeService passwordChangeService;

    @Autowired
    private UserService userService;

    /**
     * Change the given user password.
     *
     * @param username
     *              user that needs to change its password.
     * @param newPassword
     *              the new password for the user.
     */
    public void changePassword(final String username, final String newPassword) {
        final User user = userService.findByUsername(username);
        user.setCredentialsNonExpired(true);
        user.setAccountNonExpired(true);
        passwordChangeService.changePassword(user.getUsername(), newPassword);
    }

    /**
     * Validates the new password for the given user.
     *
     * @param username
     *          user that needs to change its password.
     * @param password
     *          user current password.
     * @param newPassword
     *          new password for the user.
     * @return
     *          whether the new password is valid.
     */
    public boolean validate(final String username, final String password,
                            final String newPassword) {
        final User user = userService.findByUsername(username);
        return passwordChangeService.validate(password, newPassword, user);
    }
}
