package ar.com.utn.proyecto.qremergencias.ws.controller;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.toList;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    public static final String UNEXPECTED_ERROR = "Error inesperado";
    public static final int UNEXPECTED_ERROR_CODE = HttpStatus.INTERNAL_SERVER_ERROR.value();

    public static final String BAD_INPUT = "Dato erroneo";
    public static final int BAD_INPUT_CODE = HttpStatus.BAD_REQUEST.value();

    private static final ApiError INTERNAL_SERVER_ERROR = new ApiError(UNEXPECTED_ERROR,
            1000, UNEXPECTED_ERROR_CODE);

    private static final ApiError BAD_REQUEST = new ApiError(BAD_INPUT, 1001, BAD_INPUT_CODE);

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ApiError> error(final Throwable exception) {
        log.error(exception.toString());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler({IllegalArgumentException.class,
            MissingServletRequestParameterException.class})
    public ResponseEntity<ApiError> error(final Exception exception) {
        log.error(exception.toString());
        return ResponseEntity.badRequest().body(BAD_REQUEST);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiError> error(final BindException exception) {
        log.error("BAD REQUEST", exception);
        return ResponseEntity.badRequest()
                .body(BAD_REQUEST.withFieldErrors(exception.getFieldErrors()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> error(final MethodArgumentNotValidException exception) {
        log.error("BAD REQUEST", exception);
        final BindingResult bindingResult = exception.getBindingResult();
        return ResponseEntity.badRequest()
                .body(BAD_REQUEST.withFieldErrors(bindingResult.getFieldErrors()));
    }

    @Data
    @SuppressWarnings("PMD.UnusedPrivateField")
    public static class ApiError {

        private final String message;
        private final int code;
        private final LocalDateTime timestamp = LocalDateTime.now();
        private final int status;
        private List<ApiFieldError> errors;

        public ApiError withFieldErrors(final List<FieldError> errors) {
            final ApiError apiError = new ApiError(message, code, status);
            apiError.setErrors(errors.stream().map(ApiFieldError::new).collect(toList()));
            return apiError;
        }
    }

    @Data
    @SuppressWarnings("PMD.UnusedPrivateField")
    private static class ApiFieldError {
        private final String field;
        private final Object rejectedValue;
        private final String message;

        private ApiFieldError(final FieldError error) {
            this.field = error.getField();
            this.rejectedValue = error.getRejectedValue();
            this.message = error.getDefaultMessage();
        }
    }
}