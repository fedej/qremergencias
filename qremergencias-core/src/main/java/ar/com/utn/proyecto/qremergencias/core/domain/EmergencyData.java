package ar.com.utn.proyecto.qremergencias.core.domain;

import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.mongodb.core.mapping.DBRef;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.time.LocalDate;
import java.util.List;

@Data
@SuppressWarnings("PMD.TooManyFields")
public class EmergencyData {

    @Min(0)
    @Max(150)
    private int age;

    @Length(min = 1, max = 3)
    private String bloodType;

    private char sex;
    private boolean organDonor;
    private boolean hypertension;
    private boolean heartAttack;
    private boolean extend;
    private boolean thrombosis;
    private boolean asthma;
    private boolean pneumonia;
    private boolean erectileDysfunction;
    private boolean anemia;
    private boolean psoriasis;
    private boolean smoking;
    private boolean alcoholism;
    private boolean adrenalInsufficiency;
    private List<String> allergies;
    private List<String> surgeries;
    private List<String> hospitalizations;
    private List<String> medications;
    private LocalDate lastMedicalCheck;

    @DBRef
    private UserFront user;

}
