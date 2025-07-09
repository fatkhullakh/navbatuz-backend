package uz.navbatuz.backend.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.navbatuz.backend.common.Language;
import uz.navbatuz.backend.common.Gender;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDetailsDTO {
    private String name;
    private String surname;
    private LocalDate dateOfBirth;
    private Gender gender;
    private String phoneNumber;
    private String email;
    private Language language;
}
