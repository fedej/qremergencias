package ar.com.utn.proyecto.qremergencias.core.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class UserProfileDTO {

    private String firstName;
    private String lastName;
    private String docNumber;
    private LocalDate birthDate;
    private List<UserContactDTO> contacts;

}
