package ar.com.utn.proyecto.qremergencias.core.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserFront extends User {

    private static final long serialVersionUID = -3412836946169472092L;
    private String name;
    private String lastname;

}
