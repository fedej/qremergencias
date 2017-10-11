package ar.com.utn.proyecto.qremergencias.core.service;

import ar.com.utn.proyecto.qremergencias.core.domain.ForgotPassword;
import ar.com.utn.proyecto.qremergencias.core.domain.User;
import ar.com.utn.proyecto.qremergencias.core.repository.ForgotPasswordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ForgotPasswordService {

    @Autowired
    private ForgotPasswordRepository repository;

    @Autowired
    private PasswordChangeService passwordChangeService;

    @Value("${qremergencias.forgotPassword.expirationHours}")
    private Integer expirationHours;

    public ForgotPassword create(final User user) {
        ForgotPassword forgotPassword = repository.findByUserAndExpiredFalse(user);

        if (forgotPassword == null) {
            forgotPassword = new ForgotPassword();
            forgotPassword.setUser(user);
        }

        forgotPassword.setToken(UUID.randomUUID().toString());
        forgotPassword.setExpirationTime(LocalDateTime.now().plusHours(expirationHours));
        repository.save(forgotPassword);

        return forgotPassword;
    }

    public boolean validate(final String token, final String password) {

        final ForgotPassword forgotPassword = repository.findByToken(token);

        if (forgotPassword == null) {
            return false;
        }

        if (forgotPassword.isExpired()) {
            return false;
        }

        if (forgotPassword.getExpirationTime().isBefore(LocalDateTime.now())) {
            forgotPassword.setExpired(true);
            repository.save(forgotPassword);
            return false;
        }

        if (password != null) {
            final User user = forgotPassword.getUser();

            return passwordChangeService.validate(password, user);
        }

        return true;
    }

    @Transactional
    public void changePassword(final String token, final String newPassword) {

        final ForgotPassword forgotPassword = repository.findByToken(token);
        final User user = forgotPassword.getUser();
        forgotPassword.setExpired(true);

        passwordChangeService.changePassword(user.getUsername(), newPassword);

        repository.save(forgotPassword);

    }
}
