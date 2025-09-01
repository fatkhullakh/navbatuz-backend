package uz.navbatuz.backend.staff;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import uz.navbatuz.backend.provider.repository.ProviderRepository;
import uz.navbatuz.backend.receptionist.repository.ReceptionistRepository;
import uz.navbatuz.backend.worker.repository.WorkerRepository;

import java.util.UUID;

// e.g. in a StaffGuard or ProviderService
@Component
@RequiredArgsConstructor
public class StaffGuard {
    private final ProviderRepository providerRepository;
    private final WorkerRepository workerRepository;
    private final ReceptionistRepository receptionistRepository;

    public void ensureStaffOfProvider(UUID userId, UUID providerId) {
        boolean isOwner = providerRepository.existsByIdAndOwner_Id(providerId, userId);
        boolean isWorker = workerRepository.existsByUser_IdAndProvider_Id(userId, providerId);
        boolean isReceptionist = receptionistRepository.existsByUser_IdAndProvider_Id(userId, providerId);

        if (!(isOwner || isWorker || isReceptionist)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not staff of this provider");
        }
    }
}

