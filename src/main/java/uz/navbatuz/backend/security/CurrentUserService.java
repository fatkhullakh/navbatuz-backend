package uz.navbatuz.backend.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import uz.navbatuz.backend.user.model.User;

import java.util.UUID;

@Component
public class CurrentUserService {
    public UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Unauthorized");
        }

        User user = (User) auth.getPrincipal();  // this assumes you stored User as principal
        return user.getId();
    }
}
