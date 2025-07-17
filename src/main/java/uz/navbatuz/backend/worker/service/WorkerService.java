package uz.navbatuz.backend.worker.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.navbatuz.backend.common.WorkerCategoryValidator;
import uz.navbatuz.backend.provider.model.Provider;
import uz.navbatuz.backend.provider.repository.ProviderRepository;
import uz.navbatuz.backend.security.CurrentUserService;
import uz.navbatuz.backend.user.model.User;
import uz.navbatuz.backend.user.repository.UserRepository;
import uz.navbatuz.backend.worker.dto.CreateWorkerRequest;
import uz.navbatuz.backend.worker.dto.WorkerResponse;
import uz.navbatuz.backend.worker.dto.WorkerResponseForService;
import uz.navbatuz.backend.worker.mapper.WorkerMapper;
import uz.navbatuz.backend.worker.model.Worker;
import uz.navbatuz.backend.worker.repository.WorkerRepository;
import uz.navbatuz.backend.common.Status;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
public class WorkerService {
    private final WorkerRepository workerRepository;
    private final UserRepository userRepository;
    private final ProviderRepository providerRepository;
    private final CurrentUserService currentUserService;
    private final WorkerMapper workerMapper;


    @Transactional
    public Worker createWorker(CreateWorkerRequest request) {
        UUID currentUserId = currentUserService.getCurrentUserId();

        Provider provider = providerRepository.findById(request.provider())
                .orElseThrow(() -> new RuntimeException("Provider not found"));

        if (!provider.getOwner().getId().equals(currentUserId)) {
            throw new RuntimeException("You are not allowed to assign workers to this provider.");
        }

        if (!WorkerCategoryValidator.isCompatible(provider.getCategory(), request.workerType())) {
            throw new IllegalArgumentException("Worker type " + request.workerType() + " is not allowed in " + provider.getCategory() + " category");
        }

        User user = userRepository.findById(request.user())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Worker worker = Worker.builder()
                //.id(UUID.randomUUID()) if only we want same worker to work in several shops
                .user(user)
                .provider(provider)
                .workerType(request.workerType())
                .status(Status.AVAILABLE)
                .hireDate(LocalDate.now())
                .avgRating(3.0f)
                .isActive(true)
                .build();

        return workerRepository.save(worker);
    }


    public List<WorkerResponseForService> getAllActiveWorkersOfProvider(UUID providerId) {
        return workerRepository.findByProviderIdAndIsActiveTrue(providerId)
                .stream()
                .map(workerMapper::mapToWorkerResponse)
                .toList();
    }

    public List<WorkerResponse> getAllWorkersOfProvider(UUID providerId) {
        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Provider not found"));

        return workerRepository.findByProviderId(providerId)
                .stream()
                .map(workerMapper::mapToResponse)
                .toList();
    }


//    public Worker createWorker(UUID userId, UUID providerId, WorkerType workerType) {
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//        Provider provider = providerRepository.findById(providerId)
//                .orElseThrow(() -> new RuntimeException("Provider not found"));
//
//        Worker worker = Worker.builder()
//                .user(user)
//                .provider(provider)
//                .workerType(workerType)
//                .status(Status.AVAILABLE)
//                .hireDate(LocalDate.now())
//                .terminationDate(null)
//                .avgRating(3.0f)
//                .isActive(true)
//                .build();
//
//        return workerRepository.save(worker);
//    }
//
//    public List<Worker> getAllWorkerOfProvider(UUID providerId) {
//        return workerRepository.findByProviderIdAndIsActiveTrue(providerId);
//    }

    @Transactional
    public void deactivateWorker(UUID workerId) {
        UUID currentUserId = currentUserService.getCurrentUserId();

        Worker worker = workerRepository.findById(workerId)
                .orElseThrow(() -> new RuntimeException("Worker not found"));

        if (!worker.getProvider().getOwner().getId().equals(currentUserId)) {
            throw new RuntimeException("You are not allowed to deactivate this worker.");
        }

        worker.setActive(false);
        workerRepository.save(worker);
    }

    @Transactional
    public void activateWorker(UUID workerId) {
        UUID currentUserId = currentUserService.getCurrentUserId();

        Worker worker = workerRepository.findById(workerId)
                .orElseThrow(() -> new RuntimeException("Worker not found"));

        if(!worker.getProvider().getOwner().getId().equals(currentUserId)) {
            throw new RuntimeException("You are not allowed to activate this worker.");
        }

        worker.setActive(true);
        workerRepository.save(worker);
    }

//    @Transactional
//    public Worker updateWorker(UUID workerId, Worker worker) {
//        Worker worker = workerRepository.findById(workerId)
//                .orElseThrow(() -> new RuntimeException("Worker not found"));
//
//
//    }

}
