package uz.navbatuz.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotBlank
    private String email;
    @NotBlank
    private String code;         // 6-digit OTP
    @NotBlank
    private String newPassword;  // validate length client-side
}
