package uz.navbatuz.backend.service.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uz.navbatuz.backend.common.Category;
import uz.navbatuz.backend.provider.model.Provider;
import uz.navbatuz.backend.provider.repository.ProviderRepository;
import uz.navbatuz.backend.service.dto.CreateServiceRequest;
import uz.navbatuz.backend.service.dto.ServiceResponse;
import uz.navbatuz.backend.service.dto.ServiceSummaryResponse;
import uz.navbatuz.backend.service.mapper.ServiceMapper;
import uz.navbatuz.backend.service.model.ServiceEntity;
import uz.navbatuz.backend.service.repository.ServiceRepository;
import uz.navbatuz.backend.user.model.User;
import uz.navbatuz.backend.user.repository.UserRepository;
import uz.navbatuz.backend.worker.dto.WorkerResponseForService;
import uz.navbatuz.backend.worker.mapper.WorkerMapper;
import uz.navbatuz.backend.worker.model.Worker;
import uz.navbatuz.backend.worker.repository.WorkerRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceService {

    private final ServiceRepository serviceRepository;
    private final ProviderRepository providerRepository;
    private final WorkerRepository workerRepository;
    private final ServiceMapper serviceMapper;
    private final WorkerMapper workerMapper;
    private final UserRepository userRepository;

    // ---------- READ ----------

    public List<ServiceSummaryResponse> getAllPublicServicesByProvider(UUID providerId) {
        providerRepository.findById(providerId)
                .orElseThrow(() -> new IllegalArgumentException("Provider not found"));
        return serviceRepository.findByProvider_IdAndDeletedFalseAndIsActiveTrue(providerId)
                .stream()
                .map(serviceMapper::toSummaryResponse)
                .toList();
    }

    public List<ServiceResponse> getAllServicesByProvider(UUID providerId) {
        providerRepository.findById(providerId)
                .orElseThrow(() -> new IllegalArgumentException("Provider not found"));
        return serviceRepository.findByProvider_IdAndDeletedFalse(providerId)
                .stream()
                .map(serviceMapper::toDetailedResponse)
                .toList();
    }

    public List<ServiceSummaryResponse> getAllPublicServicesByWorker(UUID workerId) {
        workerRepository.findById(workerId)
                .orElseThrow(() -> new IllegalArgumentException("Worker not found"));
        return serviceRepository.findByWorkers_IdAndDeletedFalseAndIsActiveTrue(workerId)
                .stream()
                .map(serviceMapper::toSummaryResponse)
                .toList();
    }

    public List<ServiceResponse> getAllServicesByWorker(UUID workerId) {
        workerRepository.findById(workerId)
                .orElseThrow(() -> new IllegalArgumentException("Worker not found"));
        return serviceRepository.findByWorkers_IdAndDeletedFalse(workerId)
                .stream()
                .map(serviceMapper::toDetailedResponse)
                .toList();
    }

    public ServiceResponse getService(UUID serviceId) {
        ServiceEntity serviceEntity = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));
        return serviceMapper.toDetailedResponse(serviceEntity);
    }

    // ---------- CREATE ----------

    /**
     * Owner/Admin: can pass providerId and workerIds (validated to same provider).
     * Worker/Receptionist: provider inferred from actor's worker; workerIds ignored and forced to [actorWorker].
     */
    @Transactional
    public ServiceResponse createService(CreateServiceRequest request, UUID actorId) {
        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Actor not found"));

        var role = actor.getRole();
        boolean isAdmin = role != null && "ADMIN".equals(role.name());
        boolean isReceptionist = role != null && "RECEPTIONIST".equals(role.name());
        boolean isWorker = role != null && "WORKER".equals(role.name());

        Worker actorWorker = workerRepository.findByUserId(actorId).orElse(null);

        Provider provider;

        if (isWorker || isReceptionist) {
            // 🔒 Force provider = actor's provider; ignore what client sent
            if (actorWorker == null || actorWorker.getProvider() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot infer provider for current user");
            }
            provider = actorWorker.getProvider();
        } else {
            // OWNER/ADMIN must specify providerId
            if (request.providerId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "providerId is required");
            }
            provider = providerRepository.findById(request.providerId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provider not found"));
        }

        // Resolve workers
        List<Worker> workers;
        if (isWorker || isReceptionist) {
            // 🔒 Force workers = [actorWorker]
            workers = List.of(actorWorker);
        } else {
            var ids = Optional.ofNullable(request.workerIds()).orElse(List.of());
            workers = ids.isEmpty() ? List.of() : workerRepository.findAllById(ids);
            boolean allMatch = workers.stream()
                    .allMatch(w -> w.getProvider() != null && w.getProvider().getId().equals(provider.getId()));
            if (!allMatch) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "One or more workers do not belong to the specified provider");
            }
        }

        ServiceEntity service = ServiceEntity.builder()
                .name(request.name())
                .description(request.description())
                .category(request.category())
                .price(request.price() == null ? null : BigDecimal.valueOf(request.price()))
                .duration(request.duration())
                .provider(provider)
                .workers(workers)
                .isActive(true)
                .deleted(false)
                .imageUrl(request.imageUrl())
                .build();

        service = serviceRepository.save(service);
        return serviceMapper.toDetailedResponse(service);
    }


    // ---------- UPDATE / ACTIVATE / DEACTIVATE ----------

    @Transactional
    public void deactivateById(UUID serviceId) {
        log.info("Start deactivating service with id: {}", serviceId);
        ServiceEntity s = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));
        s.setActive(false);
        serviceRepository.save(s);
        log.info("Successfully deactivated service with id: {}", serviceId);
    }

    @Transactional
    public void activateById(UUID serviceId) {
        log.info("Start activating service with id: {}", serviceId);
        ServiceEntity s = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));
        s.setActive(true);
        serviceRepository.save(s);
        log.info("Successfully activated service with id: {}", serviceId);
    }

    /**
     * Owner/Admin: may update workers list (validated to same provider).
     * Worker/Receptionist: may update basic fields & active flag ONLY on services they are assigned to;
     * cannot change workers list nor provider.
     */
    @Transactional
    public void updateServiceById(UUID serviceId, ServiceResponse request, UUID actorId) {
        ServiceEntity s = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));

        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Actor not found"));

        var role = actor.getRole();
        boolean isAdmin = role != null && "ADMIN".equals(role.name());
        boolean isOwner = s.getProvider() != null && s.getProvider().getOwner() != null
                && s.getProvider().getOwner().getId().equals(actorId);
        boolean isReceptionist = role != null && "RECEPTIONIST".equals(role.name());
        boolean isWorker = role != null && "WORKER".equals(role.name());

        // Always update basic fields
        s.setName(request.name());
        s.setDescription(request.description());
        s.setCategory(request.category());
        s.setPrice(request.price());
        s.setDuration(request.duration());
        s.setActive(request.isActive());

        if (isAdmin || isOwner) {
            // allow updating workers (must belong to same provider)
            List<Worker> workers = workerRepository.findAllById(request.workerIds());
            boolean allMatch = workers.stream()
                    .allMatch(w -> w.getProvider() != null && s.getProvider() != null
                            && w.getProvider().getId().equals(s.getProvider().getId()));
            if (!allMatch) {
                throw new IllegalArgumentException("One or more workers do not belong to the specified provider.");
            }
            s.setWorkers(workers);
        } else if (isWorker || isReceptionist) {
            // must belong to same provider and be assigned to this service
            Worker actorWorker = workerRepository.findByUserId(actorId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Worker profile not found"));

            boolean sameProvider = actorWorker.getProvider() != null && s.getProvider() != null
                    && actorWorker.getProvider().getId().equals(s.getProvider().getId());
            if (!sameProvider || s.getWorkers() == null || !s.getWorkers().contains(actorWorker)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to modify this service");
            }
            // ignore request.workerIds() – worker/receptionist cannot add/remove others
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }

        serviceRepository.save(s);
    }

    // ---------- WORKER LINKS ----------

    @Transactional
    public void addWorkerToService(UUID serviceId, UUID workerId) {
        ServiceEntity s = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));
        Worker w = workerRepository.findById(workerId)
                .orElseThrow(() -> new RuntimeException("Worker not found"));

        if (s.getWorkers().contains(w)) {
            throw new IllegalArgumentException("Worker already exists");
        }
        if (w.getProvider() == null || s.getProvider() == null
                || !w.getProvider().getId().equals(s.getProvider().getId())) {
            throw new IllegalArgumentException("Worker must belong to the same provider");
        }

        s.getWorkers().add(w);
        serviceRepository.save(s);
    }

    @Transactional
    public void removeWorkerFromService(UUID serviceId, UUID workerId) {
        ServiceEntity s = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));
        Worker w = workerRepository.findById(workerId)
                .orElseThrow(() -> new RuntimeException("Worker not found"));

        if (!s.getWorkers().contains(w)) {
            throw new RuntimeException("Worker not assigned to this service");
        }

        s.getWorkers().remove(w);
        serviceRepository.save(s);
    }

    // ---------- DELETE (soft) ----------

    @Transactional
    public void deleteService(UUID serviceId, UUID actorId) {
        ServiceEntity s = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found"));

        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Actor not found"));

        UUID ownerId = Optional.ofNullable(s.getProvider())
                .map(p -> p.getOwner())
                .map(User::getId)
                .orElse(null);

//        boolean isOwner = ownerId != null && ownerId.equals(actorId);
//        boolean isAdmin = actor.getRole() != null && "ADMIN".equals(actor.getRole().name());
//        if (!isOwner && !isAdmin) {
//            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to delete this service");
//        }

        s.setDeleted(true);
        s.setActive(false);
        serviceRepository.save(s);
    }

    // ---------- SEARCH (unchanged) ----------

    public Page<ServiceSummaryResponse> searchServices(String category, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        Category categoryEnum = null;
        if (category != null && !category.isBlank()) {
            try {
                categoryEnum = Category.valueOf(category.toUpperCase());
            } catch (IllegalArgumentException e) {
                try {
                    throw new BadRequestException("Invalid category: " + category);
                } catch (BadRequestException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
        var services = serviceRepository.searchByFilters(categoryEnum, minPrice, maxPrice, pageable);
        return services.map(serviceMapper::toSummaryResponse);
    }

    public List<WorkerResponseForService> getWorkersByServiceId(UUID serviceId) {
        ServiceEntity service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));
        return service.getWorkers()
                .stream()
                .map(workerMapper::mapToWorkerResponse)
                .toList();
    }

    // ---------- IMAGE ----------

    @Transactional
    public ServiceEntity updateImage(UUID serviceId, String url, UUID actorId) {
        ServiceEntity s = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found"));
        var actor = userRepository.findById(actorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Actor not found"));

        boolean isAdmin = actor.getRole() != null && actor.getRole().name().equals("ADMIN");
        boolean isOwner = s.getProvider() != null && s.getProvider().getOwner() != null
                && s.getProvider().getOwner().getId().equals(actorId);

        // Allow worker/receptionist of same provider
        Worker actorWorker = workerRepository.findByUserId(actorId).orElse(null);
        boolean sameProvider = actorWorker != null && actorWorker.getProvider() != null
                && s.getProvider() != null
                && actorWorker.getProvider().getId().equals(s.getProvider().getId());

        boolean isReceptionist = actor.getRole() != null && "RECEPTIONIST".equals(actor.getRole().name());
        boolean isWorker = actor.getRole() != null && "WORKER".equals(actor.getRole().name());

        if (!(isOwner || isAdmin || (sameProvider && (isWorker || isReceptionist)))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to update service image");
        }

        s.setImageUrl(url);
        return serviceRepository.save(s);
    }
}
