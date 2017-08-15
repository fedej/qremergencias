package ar.com.utn.proyecto.qremergencias.core.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.DBRef;

import javax.persistence.ManyToOne;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@javax.persistence.Entity
public class PasswordChange implements Serializable {

    @Id
    @javax.persistence.Id
    private String id;

    @Version
    private Long version;
    private LocalDateTime changeDate;
    private String password;

    @DBRef
    @ManyToOne
    private User user;

}
