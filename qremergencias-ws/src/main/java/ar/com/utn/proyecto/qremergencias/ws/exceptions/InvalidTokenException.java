package ar.com.utn.proyecto.qremergencias.ws.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class InvalidTokenException extends RuntimeException {

    public InvalidTokenException(final Exception exception) {
        super(exception);
    }

}
