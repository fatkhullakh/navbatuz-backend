// src/main/java/uz/navbatuz/backend/receptionist/service/ReceptionistService.java
package uz.navbatuz.backend.receptionist.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uz.navbatuz.backend.provider.model.Provider;
import uz.navbatuz.backend.provider.repository.ProviderRepository;
import uz.navbatuz.backend.receptionist.dto.ReceptionistCreateReq;
import uz.navbatuz.backend.receptionist.dto.ReceptionistDetailsDto;
import uz.navbatuz.backend.receptionist.dto.ReceptionistDto;
import uz.navbatuz.backend.receptionist.dto.ReceptionistUpdateReq;
import uz.navbatuz.backend.receptionist.model.Receptionist;
import uz.navbatuz.backend.receptionist.repository.ReceptionistRepository;
import uz.navbatuz.backend.security.CurrentUserService;
import uz.navbatuz.backend.user.model.User;
import uz.navbatuz.backend.user.repository.UserRepository;
import uz.navbatuz.backend.user.service.UserService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReceptionistService {

    private final ReceptionistRepository repo;
    private final UserRepository users;
    private final ProviderRepository providers;
    private final CurrentUserService current;
    private final UserRepository userRepo;

    @Transactional
    public Receptionist createReceptionist(UUID providerId, ReceptionistCreateReq req) {
        UUID currentUserId = current.getCurrentUserId();

        Provider provider = providers.findById(providerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Provider not found"));

        // SAME OWNER CHECK AS YOUR WORKER SERVICE
        if (!provider.getOwner().getId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to assign receptionists to this provider.");
        }

        User user = users.findById(req.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (repo.existsByUserIdAndProviderIdAndActiveTrue(user.getId(), providerId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already a receptionist for this provider");
        }

        Receptionist r = Receptionist.builder()
                .provider(provider)
                .user(user)
                .hireDate(req.hireDate() != null ? req.hireDate() : LocalDate.now())
                .status(Receptionist.ReceptionistStatus.ACTIVE)
                .active(true)   // <-- REQUIRED for NOT NULL
                .build();

        return repo.save(r);
    }

    public List<ReceptionistDetailsDto> list(UUID providerId) {
        return repo.findAllByProviderId(providerId)
                .stream().map(this::mapToDetailsDto).toList();
    }

    public ReceptionistDetailsDto getById(UUID receptionistId) {
        Receptionist receptionist = repo.findByIdAndActiveTrue(receptionistId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Receptionist not found"));

        return mapToDetailsDto(receptionist);
    }

    @Transactional
    public ReceptionistDetailsDto deactivate(UUID providerId, UUID receptionistId) {
        Receptionist r = repo.findByIdAndProviderId(receptionistId, providerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Receptionist not found"));
        if (!r.isActive()) return mapToDetailsDto(r);
        r.setActive(false);
        r.setStatus(Receptionist.ReceptionistStatus.TERMINATED);
        r.setTerminationDate(LocalDate.now());
        repo.save(r);
        return mapToDetailsDto(r);
    }

    @Transactional
    public ReceptionistDetailsDto activate(UUID providerId, UUID receptionistId) {
        Receptionist r = repo.findByIdAndProviderId(receptionistId, providerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Receptionist not found"));
        r.setActive(true);
        r.setStatus(Receptionist.ReceptionistStatus.ACTIVE);
        r.setTerminationDate(null);
        if (r.getHireDate() == null) r.setHireDate(LocalDate.now());
        repo.save(r);
        return mapToDetailsDto(r);
    }

    @Transactional
    public ReceptionistDetailsDto update(UUID providerId, UUID receptionistId, ReceptionistUpdateReq req) {
        Receptionist r = repo.findByIdAndProviderId(receptionistId, providerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Receptionist not found"));

        User u = r.getUser();
        if (req.name() != null && !req.name().isBlank()) u.setName(req.name().trim());
        if (req.surname() != null) u.setSurname(req.surname().trim());
        if (req.phoneNumber() != null) u.setPhoneNumber(req.phoneNumber().trim());
        if (req.email() != null) u.setEmail(req.email().trim());
        userRepo.save(u);

        return mapToDetailsDto(r);
    }

    private ReceptionistDetailsDto mapToDetailsDto(Receptionist r) {
        String providerName = (r.getProvider() != null) ? r.getProvider().getName() : null;

        return new ReceptionistDetailsDto(
                r.getId(),
                r.getUser().getName(),     // maps to `Name` in your record
                r.getUser().getSurname(),      // maps to `Surname` in your record
                providerName,
                r.getUser().getGender(),
                r.getUser().getPhoneNumber(),
                r.getUser().getEmail(),
                r.getHireDate(),
                r.isActive(),
                r.getUser().getAvatarUrl()
        );
    }

    public Optional<UUID> providerForUser(UUID userId) {
        return repo.findFirstByUserIdAndActiveTrue(userId)
                .map(r -> r.getProvider().getId());
    }

    private ReceptionistDto toDto(Receptionist r) {
        return new ReceptionistDto(
                r.getId(),
                r.getProvider().getId(),
                r.getUser().getId(),
                r.getStatus(),
                r.isActive(),
                r.getHireDate(),
                r.getTerminationDate(),
                r.getVersion()
        );
    }

    public ReceptionistDto getActiveByUserOrThrow(UUID userId) {
        var r = repo.findFirstByUserIdAndActiveTrue(userId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Receptionist not found for current user"));
        return toDto(r);
    }
}
