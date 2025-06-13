package uz.navbatuz.backend.user.repository;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import uz.navbatuz.backend.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

//    boolean existsByEmail(@NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email);
//
//    boolean existsByPhoneNumber(@NotBlank(message = "Phone number is required") @Pattern(regexp = "^\\+?998\\d{9}$", message = "Phone must be valid Uzbekistan number") String phoneNumber);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);

}
