package uz.navbatuz.backend.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import uz.navbatuz.backend.common.Role;
import uz.navbatuz.backend.user.model.User;
import uz.navbatuz.backend.user.repository.UserRepository;
import uz.navbatuz.backend.worker.model.Worker;

import java.util.UUID;

@Component
public class CurrentUserService {
    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthenticated");
        }

        Object principal = auth.getPrincipal();

        // 1) Your domain User as principal
        if (principal instanceof User u) {
            return u.getId();
        }

        // 2) Spring UserDetails (username = email)
        if (principal instanceof UserDetails ud) {
            String email = ud.getUsername();
            return userRepository.findByEmail(email)
                    .map(User::getId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        }

        // 3) Fallback: auth.getName() could be UUID or email
        String name = auth.getName();
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthenticated");
        }
        try {
            return UUID.fromString(name); // JWT subject is UUID
        } catch (IllegalArgumentException ignored) {
            // treat as email
        }
        return userRepository.findByEmail(name)
                .map(User::getId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}

//@Component
//public class CurrentUserService {
//    public UUID getCurrentUserId() {
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        if (auth == null || !auth.isAuthenticated()) {
//            throw new RuntimeException("Unauthorized");
//        }
//
//        String userIdStr = (String) auth.getPrincipal();  // ✅ safely cast
//        return UUID.fromString(userIdStr);                // ✅ convert to UUID
//    }
//}

