package uz.navbatuz.backend.provider.repository;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.navbatuz.backend.common.Category;
import uz.navbatuz.backend.provider.model.Provider;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProviderRepository extends JpaRepository<Provider, UUID> {


    boolean existsByEmail(@NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email);

    boolean existsByPhoneNumber(@NotBlank(message = "Phone number is required") @Pattern(regexp = "^\\+?998\\d{9}$", message = "Phone must be valid Uzbekistan number") String phoneNumber);

    Optional<Provider> findByEmail(String email);

    Optional<Provider> findByPhoneNumber(String phoneNumber);

    List<Provider> findAllByIsActiveTrue();

    Page<Provider> findByCategoryAndIsActiveTrue(Category category, Pageable pageable);

    Page<Provider> findByIsActiveTrue(Pageable pageable);

    Optional<Provider> findByOwnerId(UUID ownerId);

}
