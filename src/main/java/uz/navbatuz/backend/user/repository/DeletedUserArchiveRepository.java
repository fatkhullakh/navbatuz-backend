// src/main/java/uz/navbatuz/backend/user/repository/DeletedUserArchiveRepository.java
package uz.navbatuz.backend.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.navbatuz.backend.user.model.DeletedUserArchive;

import java.util.Optional;
import java.util.UUID;

public interface DeletedUserArchiveRepository extends JpaRepository<DeletedUserArchive, UUID> {
    Optional<DeletedUserArchive> findByOriginalUserId(UUID userId);
    boolean existsByOriginalUserId(UUID userId);
}
