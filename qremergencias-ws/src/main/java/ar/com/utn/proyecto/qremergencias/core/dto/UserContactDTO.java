package ar.com.utn.proyecto.qremergencias.core.dto;

import ar.com.utn.proyecto.qremergencias.core.domain.UserEmergencyContact;
import ar.com.utn.proyecto.qremergencias.core.mapper.Mapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserContactDTO {

    public static final Mapper<UserEmergencyContact, UserContactDTO> USER_CONTACT_DTO_MAPPER =
            Mapper.mapping(UserEmergencyContact.class, UserContactDTO.class)
                    .constructor(UserContactDTO::new)
                    .fields(UserEmergencyContact::getFirstName, UserContactDTO::setFirstName)
                    .fields(UserEmergencyContact::getLastName, UserContactDTO::setLastName)
                    .fields(UserEmergencyContact::getPhoneNumber, UserContactDTO::setPhoneNumber)
                    .fields(UserEmergencyContact::isPrimary, UserContactDTO::setPrimary);

    private String firstName;
    private String lastName;
    private String phoneNumber;
    private boolean primary;
}
