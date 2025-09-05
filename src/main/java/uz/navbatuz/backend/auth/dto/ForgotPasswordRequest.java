package uz.navbatuz.backend.auth.dto;

public record ForgotPasswordRequest(
        @jakarta.validation.constraints.Email @jakarta.validation.constraints.NotBlank String email
) {}
