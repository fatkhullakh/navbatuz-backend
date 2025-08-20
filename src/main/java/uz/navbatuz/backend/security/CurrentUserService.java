package uz.navbatuz.backend.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import uz.navbatuz.backend.common.Role;
import uz.navbatuz.backend.user.model.User;
import uz.navbatuz.backend.user.repository.UserRepository;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Component
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /** Returns the logged-in user's UUID (from principal or DB). */
    public UUID getCurrentUserId() {
        Authentication auth = getAuthOrThrow();

        Object principal = auth.getPrincipal();

        // Case 1: Your domain User
        if (principal instanceof User u) {
            return u.getId();
        }

        // Case 2: Spring Security UserDetails -> username = email
        if (principal instanceof UserDetails ud) {
            String email = ud.getUsername();
            return userRepository.findByEmail(email)
                    .map(User::getId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        }

        // Case 3: Fallback: name could be UUID (JWT sub) or email
        String name = auth.getName();
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthenticated");
        }
        try {
            return UUID.fromString(name);
        } catch (IllegalArgumentException ignored) {
            // Not a UUID -> treat as email
        }
        return userRepository.findByEmail(name)
                .map(User::getId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    /** Returns the logged-in user's Role (from DB or authorities as fallback). */
    public Role getCurrentUserRole() {
        Authentication auth = getAuthOrThrow();
        Object principal = auth.getPrincipal();

        // Case 1: Your domain User already present
        if (principal instanceof User u) {
            Role r = u.getRole();
            if (r == null) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User has no role");
            return r;
        }

        // Resolve via email/DB if possible
        Optional<User> fromDb = resolveUserFromPrincipalOrName(principal, auth.getName());
        if (fromDb.isPresent()) {
            Role r = fromDb.get().getRole();
            if (r == null) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User has no role");
            return r;
        }

        // Last resort: infer from GrantedAuthorities (expects ROLE_*)
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)          // e.g. "ROLE_OWNER"
                .filter(a -> a != null && a.startsWith("ROLE_"))
                .findFirst()
                .map(a -> a.substring("ROLE_".length()))      // "OWNER"
                .map(s -> {
                    try { return Role.valueOf(s.toUpperCase(Locale.ROOT)); }
                    catch (IllegalArgumentException e) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unknown role: " + s);
                    }
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "No role authority"));
    }

    // ---------- helpers ----------

    private Authentication getAuthOrThrow() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthenticated");
        }
        return auth;
    }

    private Optional<User> resolveUserFromPrincipalOrName(Object principal, String name) {
        if (principal instanceof UserDetails ud) {
            return userRepository.findByEmail(ud.getUsername());
        }
        if (name != null && !name.isBlank()) {
            try {
                // If name is UUID (JWT sub), load by id
                UUID id = UUID.fromString(name);
                return userRepository.findById(id);
            } catch (IllegalArgumentException ignored) {
                // else treat as email
                return userRepository.findByEmail(name);
            }
        }
        return Optional.empty();
    }
}
