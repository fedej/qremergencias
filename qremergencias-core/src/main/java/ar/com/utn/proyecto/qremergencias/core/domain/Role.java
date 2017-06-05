package ar.com.utn.proyecto.qremergencias.core.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;

import java.io.Serializable;

@Data
@EqualsAndHashCode(of = "authority")
@ToString(of = "authority")
public class Role implements Serializable {

    public static final String ROLE_USER = "ROLE_USER";

    private static final long serialVersionUID = 4107503809452677105L;

    @Id
    private String id;

    @NotEmpty
    private String authority;

    @Version
    private Long version;

    private boolean assignable;

}
