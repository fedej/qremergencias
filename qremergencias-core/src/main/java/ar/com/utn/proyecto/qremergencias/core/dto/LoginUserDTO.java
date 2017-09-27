package ar.com.utn.proyecto.qremergencias.core.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@SuppressWarnings({ "PMD.UnusedPrivateField", "PMD.ImmutableField", "PMD.SingularField" })
public class LoginUserDTO implements Serializable {

    private static final long serialVersionUID = 6292856074188194072L;

    private final String name;
    private final String lastName;
    private final List<String> roles;
    private final String email;

}
