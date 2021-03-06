package ar.com.utn.proyecto.qremergencias.core.service;

import ar.com.utn.proyecto.qremergencias.core.domain.User;
import ar.com.utn.proyecto.qremergencias.core.domain.UserVerificationToken;
import ar.com.utn.proyecto.qremergencias.core.repository.UserRepository;
import ar.com.utn.proyecto.qremergencias.core.repository.UserVerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@SuppressWarnings("PMD.TooManyMethods")
public class UserService {

    private static final String ADMIN = "admin";

    @Autowired
    @Lazy
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserVerificationTokenRepository userTokenRepository;

    public <T extends User> T save(final T user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.insert(user);
    }

    public User update(final User user) {
        final User dbUser = userRepository.findOne(user.getId());

        if (dbUser.getRoles().contains("ROLE_ADMIN")) {
            return null;
        }

        dbUser.setEnabled(user.isEnabled());
        dbUser.setAccountNonLocked(user.isAccountNonLocked());
        dbUser.setAccountNonExpired(user.isAccountNonExpired());
        dbUser.setCredentialsNonExpired(user.isCredentialsNonExpired());

        return userRepository.save(dbUser);
    }

    public Page<User> findAll(final Pageable page) {
        return userRepository.findAll(page);
    }

    public Page<User> findByRole(final String role, final Pageable page) {
        return userRepository.findByRolesContaining(role, page);
    }

    public User findByUsername(final String username) {
        return userRepository.findByUsername(username);
    }

    public User findById(final String id) {
        return userRepository.findOne(id);
    }

    @Transactional
    public boolean delete(final String id, final User currentUser) {

        final User user = userRepository.findOne(id);

        if (user.getRoles().contains("ROLE_ADMIN") || user.equals(currentUser)) {
            return false;
        }

        userRepository.delete(user);

        return true;
    }

    public boolean blockUser(final String username) {
        final User user = findByUsername(username);

        if (user != null) {
            user.setAccountNonLocked(false);
            userRepository.save(user);
        }
        return user != null;
    }

    public boolean isNotAdmin(final String adminPass) {

        if (StringUtils.isEmpty(adminPass)) {
            return true;
        }

        final User admin = userRepository.findByUsername(ADMIN);
        return !passwordEncoder.matches(adminPass, admin.getPassword());
    }

    public boolean exists(final String username) {
        return userRepository.findByUsername(username) != null;
    }


    public void createVerificationToken(final User user, final String token) {
        final UserVerificationToken uvt = new UserVerificationToken(user, token);
        userTokenRepository.save(uvt);
    }

    public UserVerificationToken getUserVerificationByToken(final String token) {
        return userTokenRepository.findByToken(token);
    }

    public UserVerificationToken getUserVerificationByUser(final User user) {
        return userTokenRepository.findByUser(user);
    }

    public void deleteVerificationToken(final UserVerificationToken userVerificationToken) {
        userTokenRepository.delete(userVerificationToken);
    }
}
