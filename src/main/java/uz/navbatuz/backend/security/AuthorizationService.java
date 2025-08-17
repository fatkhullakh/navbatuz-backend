package uz.navbatuz.backend.security;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uz.navbatuz.backend.common.Role;
import uz.navbatuz.backend.provider.model.Provider;
import uz.navbatuz.backend.provider.repository.ProviderRepository;
import uz.navbatuz.backend.user.model.User;
import uz.navbatuz.backend.worker.model.Worker;
import uz.navbatuz.backend.worker.repository.WorkerRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthorizationService {
    private final CurrentUserService currentUserService;
    private final ProviderRepository providerRepository;
    private final WorkerRepository workerRepository;


    public boolean canModifyWorker(User currentUser, Worker worker) {
        Role role = currentUser.getRole();
        if (role == Role.ADMIN) return true;
        if (role == Role.OWNER && worker.getProvider().getOwner().getId().equals(currentUser.getId())) return true;
        //if (role == Role.RECEPTIONIST && worker.getProvider().hasReceptionist(currentUser)) return true;
        if (role == Role.WORKER && worker.getUser().getId().equals(currentUser.getId())) return true;
        return false;
    }

    public void ensureStaffOfProvider(UUID providerId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        if (isAdmin) return;

        UUID userId = currentUserService.getCurrentUserId();

        Provider p = providerRepository.findById(providerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Provider not found"));

        // Owner?
        if (p.getOwner() != null && p.getOwner().getId().equals(userId)) return;

        // Worker of this provider?
        boolean isWorkerHere = workerRepository.existsByUserIdAndProviderId(userId, providerId);
        if (isWorkerHere) return;

        // (Add receptionist check later if you model it)

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not staff of this provider");
    }




}
