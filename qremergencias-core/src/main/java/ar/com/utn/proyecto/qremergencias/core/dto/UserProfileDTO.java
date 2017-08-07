package ar.com.utn.proyecto.qremergencias.core.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class UserProfileDTO {

    private String firstName;
    private String lastName;
    private String idNumber;
    private LocalDate birthDate;
    private char sex;
    private List<UserContactDTO> contacts;

}
