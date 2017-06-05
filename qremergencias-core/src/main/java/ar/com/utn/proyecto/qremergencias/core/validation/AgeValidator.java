package ar.com.utn.proyecto.qremergencias.core.validation;

import java.time.LocalDate;

public class AgeValidator {

    private final int minAge;

    public AgeValidator(final int minAge) {
        this.minAge = minAge;
    }

    public boolean validate(final int day, final int month, final int year) {
        final LocalDate birthday = LocalDate.of(year, month, day);
        final LocalDate today = LocalDate.now();
        return today.compareTo(birthday.plusYears(minAge)) >= 0;
    }

}
