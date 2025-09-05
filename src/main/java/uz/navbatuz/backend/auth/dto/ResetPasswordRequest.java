package uz.navbatuz.backend.auth.dto;

public record ResetPasswordRequest(
        @jakarta.validation.constraints.Email @jakarta.validation.constraints.NotBlank String email,
        @jakarta.validation.constraints.Pattern(regexp="^[0-9]{6}$") String code,
        @jakarta.validation.constraints.Size(min=8, max=128) String newPassword
) {}
