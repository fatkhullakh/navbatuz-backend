package uz.navbatuz.backend.auth.dto;
import lombok.*;

@Data
public class RegisterRequest {
    private String name;
    private String surname;
    private String email;
    private String phoneNumber;
    private String password;
}
