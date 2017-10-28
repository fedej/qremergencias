package ar.com.utn.proyecto.qremergencias.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserContactDTO {

    private String firstName;
    private String lastName;
    private String phoneNumber;
    private boolean primary;
}
