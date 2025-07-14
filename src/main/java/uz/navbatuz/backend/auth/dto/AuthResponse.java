package uz.navbatuz.backend.auth.dto;
import lombok.AllArgsConstructor;
import lombok.Data;


// Authentication refers to the process of verifying the identity of a user,
// based on provided credentials. A common example is entering a username and
// a password when you log in to a website. You can think of it as an answer
// to the question Who are you?.
//
// Authorization refers to the process of determining if a user has proper permission
// to perform a particular action or read particular data, assuming that the user is
// successfully authenticated. You can think of it as an answer to the question Can a user do/read this?.

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
}
