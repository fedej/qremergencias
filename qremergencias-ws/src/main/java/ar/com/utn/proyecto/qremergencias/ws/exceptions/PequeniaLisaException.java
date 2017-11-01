package ar.com.utn.proyecto.qremergencias.ws.exceptions;

/**
 * Created by proyecto on 10/8/17.
 */
public class PequeniaLisaException extends RuntimeException {

    public PequeniaLisaException(final Exception exception) {
        super(exception);
    }

}
