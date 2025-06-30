package uz.navbatuz.backend.user.dto;

import lombok.Data;
import uz.navbatuz.backend.user.model.Gender;

@Data
public class UserResponseForWorker {

    private String name;
    private String surname;
    private Gender gender;
    private String phoneNumber;
}
