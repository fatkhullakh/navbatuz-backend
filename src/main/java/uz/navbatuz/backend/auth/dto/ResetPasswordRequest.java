package uz.navbatuz.backend.auth.dto;

import jakarta.validation.constraints.*;

public record ResetPasswordRequest(
        @Email @NotBlank String email,
        @Pattern(regexp = "^[0-9]{6}$", message = "Invalid code") String code,
        @Size(min = 6, max = 128) String newPassword
) {}
