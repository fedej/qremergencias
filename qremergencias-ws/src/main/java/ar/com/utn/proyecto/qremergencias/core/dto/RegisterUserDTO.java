package ar.com.utn.proyecto.qremergencias.core.dto;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import ar.com.utn.proyecto.qremergencias.core.validation.Captcha;
import lombok.Data;

@Data
@SuppressWarnings({ "PMD.UnusedPrivateField", "PMD.ImmutableField" })
public class RegisterUserDTO {

    @AssertTrue
    private boolean tyc;

    @Captcha
    private String recaptchaResponse;

    @NotNull
    @Min(1)
    @Max(31)
    private Integer day;

    @NotNull
    @Min(1)
    @Max(12)
    private Integer month;

    @NotNull
    @Min(1900)
    @Max(2100)
    private Integer year;

}
