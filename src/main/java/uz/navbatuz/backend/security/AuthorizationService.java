package uz.navbatuz.backend.security;

import org.springframework.stereotype.Component;
import uz.navbatuz.backend.common.Role;
import uz.navbatuz.backend.user.model.User;
import uz.navbatuz.backend.worker.model.Worker;

@Component
public class AuthorizationService {

    public boolean canModifyWorker(User currentUser, Worker worker) {
        Role role = currentUser.getRole();
        if (role == Role.ADMIN) return true;
        if (role == Role.OWNER && worker.getProvider().getOwner().getId().equals(currentUser.getId())) return true;
        //if (role == Role.RECEPTIONIST && worker.getProvider().hasReceptionist(currentUser)) return true;
        if (role == Role.WORKER && worker.getUser().getId().equals(currentUser.getId())) return true;
        return false;
    }
}
