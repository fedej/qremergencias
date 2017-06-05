package ar.com.utn.proyecto.qremergencias.ws.controller;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Date;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final ApiError INTERNAL_SERVER_ERROR = new ApiError("Error inesperado",
            1001, HttpStatus.INTERNAL_SERVER_ERROR.value());
    private static final ApiError BAD_REQUEST = new ApiError("Dato erroneo",
            1002, HttpStatus.BAD_REQUEST.value());

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ApiError> error(final Throwable exception) {
        log.error(exception.toString());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler({BindException.class, IllegalArgumentException.class,
            MissingServletRequestParameterException.class})
    public ResponseEntity<ApiError> error(final Exception exception) {
        log.error(exception.toString());
        return ResponseEntity.badRequest().body(BAD_REQUEST);

    }

    @Data
    @SuppressWarnings("PMD.UnusedPrivateField")
    private static class ApiError {
        private final String error;
        private final int code;
        private final Date timestamp = new Date();
        private final int status;
    }

}