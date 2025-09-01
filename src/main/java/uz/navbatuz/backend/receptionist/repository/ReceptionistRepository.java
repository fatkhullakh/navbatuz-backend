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

    List<Receptionist> findAllByProviderId(UUID providerId);

    Optional<Receptionist> findFirstByUserIdAndActiveTrue(UUID userId);

    Optional<Receptionist> findByProviderIdAndActiveTrue(UUID providerId);

    Optional<Receptionist> findByIdAndActiveTrue(UUID id);

    Optional<Receptionist> findByIdAndProviderId(UUID id, UUID providerId);

    Optional<Receptionist> findByProviderId(UUID userId);

    Optional<Receptionist> findByUserId(UUID userId);


    boolean existsByUser_IdAndProvider_Id(UUID userId, UUID providerId);
}
