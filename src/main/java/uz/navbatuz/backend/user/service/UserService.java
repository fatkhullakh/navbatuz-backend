package uz.navbatuz.backend.user.service;


import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uz.navbatuz.backend.location.dto.LocationRequest;
import uz.navbatuz.backend.location.model.Location;
import uz.navbatuz.backend.provider.model.Provider;
import uz.navbatuz.backend.provider.service.ProviderService;
import uz.navbatuz.backend.user.dto.ChangePasswordRequest;
import uz.navbatuz.backend.user.dto.SettingsUpdateRequest;
import uz.navbatuz.backend.user.dto.UserDetailsDTO;
import uz.navbatuz.backend.user.model.User;
import uz.navbatuz.backend.user.repository.UserRepository;

import java.util.List;
import java.util.UUID;

import static uz.navbatuz.backend.provider.service.ProviderService.makePoint;
import static uz.navbatuz.backend.provider.service.ProviderService.normIso2;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProviderService providerService;

    @Override
    public UserDetails loadUserByUsername(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public List<UserDetailsDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> new UserDetailsDTO(
                        user.getId(),
                        user.getName(),
                        user.getSurname(),
                        user.getDateOfBirth(),
                        user.getGender(),
                        user.getPhoneNumber(),
                        user.getEmail(),
                        user.getLanguage(),
                        user.getCountry(),
                        user.getAvatarUrl()
                ))
                .toList();
    }

    public UserDetailsDTO getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return new UserDetailsDTO(
                user.getId(),
                user.getName(),
                user.getSurname(),
                user.getDateOfBirth(),
                user.getGender(),
                user.getPhoneNumber(),
                user.getEmail(),
                user.getLanguage(),
                user.getCountry(),
                user.getAvatarUrl()
        );
    }

    public void deactivateUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setActive(false);
        userRepository.save(user);
    }

    public void changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    public UserDetailsDTO getUserByEmail(String email) {
        var u = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return toDto(u);
    }

    private UserDetailsDTO toDto(User u) {
        var dto = new UserDetailsDTO();
        dto.setId(u.getId());
        dto.setName(u.getName());
        dto.setSurname(u.getSurname());
        dto.setDateOfBirth(u.getDateOfBirth());
        dto.setGender(u.getGender());
        dto.setPhoneNumber(u.getPhoneNumber());
        dto.setEmail(u.getEmail());
        dto.setLanguage(u.getLanguage());
        dto.setCountry(u.getCountry());
        dto.setAvatarUrl(u.getAvatarUrl()); // <-- NEW
        return dto;
    }

//    public UUID findIdByEmail(String email) {
//        User u = userRepository.findByEmail(email)
//                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
//        return u.getId();
//    }

    public UserDetailsDTO updateSettingsById(UUID id, SettingsUpdateRequest req) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (req.getLanguage() != null) {
            user.setLanguage(req.getLanguage()); // enum
        }
        if (req.getCountry() != null && !req.getCountry().isBlank()) {
            user.setCountry(req.getCountry().trim().toUpperCase());
        }
        userRepository.save(user);

        return new UserDetailsDTO(
                user.getId(),
                user.getName(),
                user.getSurname(),
                user.getDateOfBirth(),
                user.getGender(),
                user.getPhoneNumber(),
                user.getEmail(),
                user.getLanguage(),
                user.getCountry(),
                user.getAvatarUrl()
        );
    }


    // CHANGE PASSWORD BY UUID
    public void changePasswordById(UUID id, ChangePasswordRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    // OPTIONAL: make sure updateUserById also updates country if provided
    public void updateUserById(UUID id, UserDetailsDTO request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        userRepository.findByEmail(request.getEmail()).ifPresent((User existing) -> {
            if (!existing.getId().equals(id)) {
                throw new IllegalArgumentException("A user with this email already exists.");
            }
        });

        userRepository.findByPhoneNumber(request.getPhoneNumber()).ifPresent((User existing) -> {
            if (!existing.getId().equals(id)) {
                throw new IllegalArgumentException("A user with this phone number already exists.");
            }
        });

        user.setName(request.getName());
        user.setSurname(request.getSurname());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setGender(request.getGender());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setEmail(request.getEmail());

        userRepository.save(user);
    }

    public User requireByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    public UUID findIdByEmail(String email) {
        return requireByEmail(email).getId();
    }

    /** Robustly resolve current user's UUID from Authentication (email or UUID principal). */
    public UUID requireUserIdByAuth(Authentication auth) {
        if (auth == null || auth.name() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthenticated");
        }
        String principal = auth.name();

        // If your JWT subject is UUID, accept it directly.
        try {
            return UUID.fromString(principal);
        } catch (IllegalArgumentException ignore) {
            // Otherwise, treat it as email.
        }

        return findIdByEmail(principal);
    }

    @Transactional
    public void updateAvatarUrl(UUID userId, String url, String requesterEmail) {
        var u = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Optional guard: only the owner (or admin) can update their avatar
        if (!requesterEmail.equalsIgnoreCase(u.getEmail())) {
            // add your own admin/role check if needed
            throw new AccessDeniedException("Not allowed");
        }

        u.setAvatarUrl(url);
        userRepository.save(u);
    }

    @Transactional
    public void removeAvatar(UUID userId, String requesterEmail) {
        var u = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // only the owner (or add admin check if you need)
        if (!requesterEmail.equalsIgnoreCase(u.getEmail())) {
            throw new AccessDeniedException("Not allowed");
        }

        // Minimal: just null out the URL (frontend will hide the image)
        u.setAvatarUrl(null);
        userRepository.save(u);

        // If you later want to also delete the file from disk/S3,
        // you can add best-effort deletion here.
    }
}