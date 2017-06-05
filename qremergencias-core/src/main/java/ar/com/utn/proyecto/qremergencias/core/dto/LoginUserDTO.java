package ar.com.utn.proyecto.qremergencias.core.dto;

import java.io.Serializable;

import lombok.Data;

@Data
@SuppressWarnings({ "PMD.UnusedPrivateField", "PMD.ImmutableField", "PMD.SingularField" })
public class LoginUserDTO implements Serializable {

    private static final long serialVersionUID = 6292856074188194072L;

    private final String name;
    private final String lastName;
    private final String imageUrl;
}
