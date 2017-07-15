package ar.com.utn.proyecto.qremergencias.core.domain;

import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@SuppressWarnings("PMD.ImmutableField")
public class UserVerificationToken {

    private static final int EXPIRATION = 60 * 24;

    @Id
    private String id;

    @NotEmpty
    private String token;

    @DBRef
    private User user;

    @NotNull
    private LocalDateTime expiryDate;

    public UserVerificationToken(final User user, final String token) {
        setUser(user);
        setToken(token);
        setExpiryDate(calculateExpiryDate());
    }

    private LocalDateTime calculateExpiryDate() {
        return LocalDateTime.now().plusHours(24);
    }


}
