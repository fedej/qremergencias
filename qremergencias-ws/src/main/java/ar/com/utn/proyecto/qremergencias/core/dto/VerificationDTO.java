package ar.com.utn.proyecto.qremergencias.core.dto;

import lombok.Data;

@Data
public class VerificationDTO {
    private final String user;
    private final String errorMessage;
}
