package ar.com.utn.proyecto.qremergencias.core.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserProfileDTO {

    private String firstName;
    private String lastName;
    private String idNumber;
    private LocalDateTime birthDate;
    private char sex;
    private List<UserContactDTO> contacts;

}
