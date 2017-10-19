package ar.com.utn.proyecto.qremergencias.core.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DoctorFront extends UserFront {

    private String registrationNumber;
    private Object evidenceFile;
    private boolean verified;

    @Override
    public boolean isEnabled() {
        return super.isEnabled() & this.isVerified();
    }
}
