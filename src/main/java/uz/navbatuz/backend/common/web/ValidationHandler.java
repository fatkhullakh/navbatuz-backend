package uz.navbatuz.backend.common.web;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.ConstraintViolationException;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ValidationHandler {

    // @Valid on @RequestBody → MethodArgumentNotValidException
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY) // 422
    public Map<String, String> handleBodyValidation(MethodArgumentNotValidException ex) {
        return ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fe -> fe.getDefaultMessage() == null ? "invalid" : fe.getDefaultMessage(),
                        (a,b) -> a // keep first
                ));
    }

    // @Validated on @RequestParam/@PathVariable → ConstraintViolationException
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public Map<String, String> handleConstraintViolations(ConstraintViolationException ex) {
        return Map.of("error", "validation_failed");
    }
}
