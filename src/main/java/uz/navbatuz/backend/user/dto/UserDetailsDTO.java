package uz.navbatuz.backend.user.dto;

import io.micrometer.common.lang.Nullable;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.navbatuz.backend.common.Language;
import uz.navbatuz.backend.common.Gender;

import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDetailsDTO {
    private UUID id;                 // <--- add
    private String name;
    private String surname;
    private LocalDate dateOfBirth;
    private Gender gender;
    private String phoneNumber;
    private String email;
    private Language language;
    private String country;          // if you store it

    // getters/setters
}