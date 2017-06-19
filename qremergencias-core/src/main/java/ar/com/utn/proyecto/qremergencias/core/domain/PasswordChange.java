package ar.com.utn.proyecto.qremergencias.core.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class PasswordChange implements Serializable {

    @Id
    private String id;

    @Version
    private Long version;
    private LocalDateTime changeDate;
    private String password;

    @DBRef
    private User user;

}
