package ar.com.utn.proyecto.qremergencias.ws.controller;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.BasicErrorController;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.stream.Collectors.toList;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends BasicErrorController {

    public static final String UNEXPECTED_ERROR = "Error inesperado";
    public static final int UNEXPECTED_ERROR_CODE = HttpStatus.INTERNAL_SERVER_ERROR.value();

    public static final String BAD_INPUT = "Dato erroneo";
    public static final int BAD_INPUT_CODE = HttpStatus.BAD_REQUEST.value();

    private static final ApiError INTERNAL_SERVER_ERROR = new ApiError(UNEXPECTED_ERROR,
            1000, UNEXPECTED_ERROR_CODE);

    private static final ApiError BAD_REQUEST = new ApiError(BAD_INPUT, 1001, BAD_INPUT_CODE);

    private static final ApiError MVC_ERROR = new ApiError("Error FWK", 1002, 0);

    @Autowired
    public GlobalExceptionHandler(final ErrorAttributes errorAttributes,
                                  final ServerProperties serverProperties) {
        super(errorAttributes, serverProperties.getError());
    }

    @RequestMapping
    @ResponseBody
    @Override
    public ResponseEntity<Map<String, Object>> error(final HttpServletRequest request) {
        final Map<String, Object> body = super.getErrorAttributes(request,
                isIncludeStackTrace(request, MediaType.ALL));
        log.error(body.toString());
        return ResponseEntity.status(getStatus(request)).body(ApiError.toMap(MVC_ERROR));
    }

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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> mvcError(final Exception exception) {
        log.error(exception.toString());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(INTERNAL_SERVER_ERROR);
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

        private static Map<String, Object> toMap(final ApiError error) {
            final Map<String, Object> asMap = new ConcurrentHashMap<>(5);
            asMap.put("message", error.message);
            asMap.put("code", error.code);
            asMap.put("timestamp", error.timestamp);
            asMap.put("status", error.status);
            asMap.put("errors", error.errors);
            return asMap;
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