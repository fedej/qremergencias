package ar.com.utn.proyecto.qremergencias.core.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.DBRef;

import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@javax.persistence.Entity
public class ForgotPassword implements Serializable {

    @Id
    @javax.persistence.Id
    private String id;

    @Version
    private Long version;

    @NotNull
    private String token;

    private boolean expired;

    @NotNull
    private LocalDateTime expirationTime;

    @DBRef
    @ManyToOne
    private User user;

}
