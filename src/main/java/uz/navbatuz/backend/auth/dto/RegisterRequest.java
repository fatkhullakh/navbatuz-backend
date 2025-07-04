package uz.navbatuz.backend.auth.dto;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import uz.navbatuz.backend.common.Language;
import uz.navbatuz.backend.user.model.Gender;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class RegisterRequest {
    private String name;
    private String surname;
    private LocalDateTime created_at;
    private LocalDate dateOfBirth;
    private Gender gender;
    private String phoneNumber;
    private String email;
    private String password;
    private Language language;
}
