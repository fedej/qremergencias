package ar.com.utn.proyecto.qremergencias.core.service;

import ar.com.utn.proyecto.qremergencias.core.domain.PasswordChange;
import ar.com.utn.proyecto.qremergencias.core.domain.User;
import ar.com.utn.proyecto.qremergencias.core.repository.PasswordChangeRepository;
import ar.com.utn.proyecto.qremergencias.core.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class PasswordChangeService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordChangeRepository passwordChangeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public boolean validate(final String currentPassword, final String newPassword,
                            final User user) {
        return passwordEncoder.matches(currentPassword, user.getPassword())
                && !newPassword.equals(currentPassword)
                && validate(newPassword, user);
    }

    public boolean validate(final String password, final User user) {

        if (password == null) {
            return false;
        }

        final Page<PasswordChange> changesPage = passwordChangeRepository
                .findByUser(user, new PageRequest(0, 1, Sort.Direction.DESC, "changeDate"));
        
        for (final PasswordChange passwordChange : changesPage.getContent()) {
            if (passwordEncoder.matches(password,passwordChange.getPassword())) {
                return false;
            }
        }

        return true;

    }

    @Transactional
    public void changePassword(final String username, final String newPassword) {
        final User user = userRepository.findByUsername(username);
        final PasswordChange pc = new PasswordChange();
        pc.setChangeDate(LocalDateTime.now());
        pc.setPassword(user.getPassword());
        user.setPassword(passwordEncoder.encode(newPassword));
        pc.setUser(user);
        userRepository.save(user);
        passwordChangeRepository.save(pc);

    }
}
