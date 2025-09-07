package uz.navbatuz.backend.config;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.navbatuz.backend.common.AccountDeletedOrDisabledException;

import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody Map<String, String> handleBadRequest(IllegalArgumentException ex) {
        return Map.of("error", ex.getMessage());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public @ResponseBody Map<String, String> handleNotFound(EntityNotFoundException ex) {
        return Map.of("error", ex.getMessage());
    }

    @ExceptionHandler(AccountDeletedOrDisabledException.class)
    public ResponseEntity<?> handleDeleted(AccountDeletedOrDisabledException ex) {
        // 410 Gone is explicit; 423 Locked is another option.
        return ResponseEntity.status(HttpStatus.GONE)
                .body(new ApiError("account_deleted", ex.getMessage()));
    }

    static record ApiError(String code, String message) {}
}
