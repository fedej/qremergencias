package ar.com.utn.proyecto.qremergencias.core.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.DBRef;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class ForgotPassword implements Serializable {

    @Id
    private String id;

    @Version
    private Long version;

    @NotNull
    private String token;

    private boolean expired;

    @NotNull
    private LocalDateTime expirationTime;

    @DBRef
    private User user;

}
