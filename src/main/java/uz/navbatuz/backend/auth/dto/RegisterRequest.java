package uz.navbatuz.backend.auth.dto;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import uz.navbatuz.backend.common.Language;
import uz.navbatuz.backend.common.Role;
import uz.navbatuz.backend.common.Gender;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class RegisterRequest {

     @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Surname is required")
    private String surname;

    private LocalDateTime created_at;

    private LocalDate dateOfBirth;
    private Gender gender;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?998\\d{9}$", message = "Phone must be valid Uzbekistan number")
    private String phoneNumber;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password too short")
    private String password;

    private Language language;
    private Role role;
}
