package uz.navbatuz.backend.user.repository;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.stereotype.Repository;
import uz.navbatuz.backend.provider.model.Provider;
import uz.navbatuz.backend.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    Optional<User> findById(UUID id);

    //    boolean existsByEmail(@NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email);
//
//    boolean existsByPhoneNumber(@NotBlank(message = "Phone number is required") @Pattern(regexp = "^\\+?998\\d{9}$", message = "Phone must be valid Uzbekistan number") String phoneNumber);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);

    Optional<User> findByPhoneNumber(String phoneNumber);
}
