package ar.com.utn.proyecto.qremergencias.bo.dto;

import lombok.Data;

import java.util.List;

@Data
@SuppressWarnings("PMD.UnusedPrivateField")
public class EditUserDTO {

    private String id;
    private boolean accountNonLocked;
    private boolean credentialsNonExpired;
    private boolean enabled;
    private List<String> roles;

}
