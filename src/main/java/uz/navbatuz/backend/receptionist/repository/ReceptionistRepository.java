// src/main/java/uz/navbatuz/backend/receptionist/repository/ReceptionistRepository.java
package uz.navbatuz.backend.receptionist.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.navbatuz.backend.receptionist.dto.ReceptionistDetailsDto;
import uz.navbatuz.backend.receptionist.model.Receptionist;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReceptionistRepository extends JpaRepository<Receptionist, UUID> {

    boolean existsByUserIdAndProviderIdAndActiveTrue(UUID userId, UUID providerId);

    List<Receptionist> findAllByProviderIdAndActiveTrue(UUID providerId);

    Optional<Receptionist> findFirstByUserIdAndActiveTrue(UUID userId);

    Optional<Receptionist> findByProviderIdAndActiveTrue(UUID providerId);

    Optional<Receptionist> findByIdAndActiveTrue(UUID id);
}
