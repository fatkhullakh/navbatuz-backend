package uz.navbatuz.backend.service.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uz.navbatuz.backend.common.Category;
import uz.navbatuz.backend.provider.model.Provider;
import uz.navbatuz.backend.provider.repository.ProviderRepository;
import uz.navbatuz.backend.service.dto.CreateServiceRequest;
import uz.navbatuz.backend.service.dto.ServiceDetailedResponse;
import uz.navbatuz.backend.service.dto.ServiceResponse;
import uz.navbatuz.backend.service.dto.ServiceSummaryResponse;
import uz.navbatuz.backend.service.mapper.ServiceMapper;
import uz.navbatuz.backend.service.model.ServiceEntity;
import uz.navbatuz.backend.service.repository.ServiceRepository;
import uz.navbatuz.backend.user.model.User;
import uz.navbatuz.backend.user.repository.UserRepository;
import uz.navbatuz.backend.worker.dto.WorkerResponse;
import uz.navbatuz.backend.worker.dto.WorkerResponseForService;
import uz.navbatuz.backend.worker.mapper.WorkerMapper;
import uz.navbatuz.backend.worker.model.Worker;
import uz.navbatuz.backend.worker.repository.WorkerRepository;

import javax.management.ServiceNotFoundException;
import java.math.BigDecimal;
import java.util.List;
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

//    @Transactional
//    public ServiceResponse create(CreateServiceRequest request) {
//        Provider provider = providerRepository.findById(request.providerId())
//                .orElseThrow(() -> new RuntimeException("Provider not found"));
//
//        List<Worker> workers = workerRepository.findAllById(request.workerIds());
//
//        ServiceEntity service = ServiceEntity.builder()
//                .name(request.name())
//                .description(request.description())
//                .category(request.category())
//                .price(BigDecimal.valueOf(request.price()))
//                .duration(request.duration())
//                .isActive(true)
//                .provider(provider)
//                .workers(workers)
//                .build();
//
//        ServiceEntity saved = serviceRepository.save(service);
//        return mapToResponse(saved);
//    }


    public List<ServiceSummaryResponse> getAllPublicServicesByProvider(UUID providerId) {
        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new IllegalArgumentException("Provider not found"));

        return serviceRepository.findByProviderIdAndIsActiveTrue(providerId)
                .stream()
                .map(serviceMapper::toSummaryResponse)
                .toList();
    }

    public List<ServiceResponse> getAllServicesByProvider(UUID providerId) {
        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new IllegalArgumentException("Provider not found"));

        return serviceRepository.findByProviderId(providerId)
                .stream()
                .map(serviceMapper::toDetailedResponse)
                .toList();
    }

    public List<ServiceSummaryResponse> getAllPublicServicesByWorker(UUID workerId) {
        Worker worker = workerRepository.findById(workerId)
                .orElseThrow(() -> new IllegalArgumentException("Worker not found"));

        return serviceRepository.findByWorkerIdAndIsActiveTrue(workerId)
                .stream()
                .map(serviceMapper::toSummaryResponse)
                .toList();
    }

    public List<ServiceResponse> getAllServicesByWorker(UUID workerId) {
        Worker worker = workerRepository.findById(workerId)
                .orElseThrow(() -> new IllegalArgumentException("Worker not found"));

        return serviceRepository.findByWorkerId(workerId)
                .stream()
                .map(serviceMapper::toDetailedResponse)
                .toList();
    }

    public ServiceResponse getService(UUID serviceId) {
        ServiceEntity serviceEntity = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));

        return serviceMapper.toDetailedResponse(serviceEntity);
    }


    @Transactional
    public ServiceResponse createService(CreateServiceRequest request) {
        Provider provider = providerRepository.findById(request.providerId())
                .orElseThrow(() -> new IllegalArgumentException("Provider not found"));

        List<Worker> workers = workerRepository.findAllById(request.workerIds());

        boolean allMatch = workers.stream()
                .allMatch(w -> w.getProvider().getId().equals(provider.getId()));

        if (!allMatch) {
            throw new IllegalArgumentException("One or more workers do not belong to the specified provider.");
        }

        ServiceEntity service = ServiceEntity.builder()
                .name(request.name())
                .description(request.description())
                .category(request.category())
                .price(request.price() != null ? java.math.BigDecimal.valueOf(request.price()) : null)
                .duration(request.duration())
                .provider(provider)
                .workers(workers)
                .isActive(true)
                .build();

        service = serviceRepository.save(service);

        return serviceMapper.toDetailedResponse(service); // return DTO
    }

    @Transactional
    public void deactivateById(UUID serviceId) {
        log.info("Start deactivating service with id: {}", serviceId);
        ServiceEntity serviceEntity = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));
        serviceEntity.setActive(false);
        serviceRepository.save(serviceEntity);
        log.info("Successfully deactivated service with id: {}", serviceId);
    }

    @Transactional
    public void activateById(UUID serviceId) {
        log.info("Start activating service with id: {}", serviceId);
        ServiceEntity serviceEntity = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));
        serviceEntity.setActive(true);
        serviceRepository.save(serviceEntity);
        log.info("Successfully activated service with id: {}", serviceId);
    }

    @Transactional
    public void updateServiceById(UUID serviceId, ServiceResponse request) {
        ServiceEntity serviceEntity = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));

        Provider provider = providerRepository.findById(request.providerId())
                .orElseThrow(() -> new IllegalArgumentException("Provider not found"));

        List<Worker> workers = workerRepository.findAllById(request.workerIds());

        boolean allMatch = workers.stream()
                .allMatch(w -> w.getProvider().getId().equals(provider.getId()));

        if (!allMatch) {
            throw new IllegalArgumentException("One or more workers do not belong to the specified provider.");
        }

        serviceEntity.setName(request.name());
        serviceEntity.setDescription(request.description());
        serviceEntity.setCategory(request.category());
        serviceEntity.setPrice(request.price());
        serviceEntity.setDuration(request.duration());
        serviceEntity.setActive(request.isActive());
        serviceEntity.setWorkers(workers);
        serviceRepository.save(serviceEntity);
    }


    @Transactional
    public void addWorkerToService(UUID serviceId, UUID workerId) {
        ServiceEntity service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));

        Worker worker = workerRepository.findById(workerId)
                .orElseThrow(() -> new RuntimeException("Worker not found"));

        if(service.getWorkers().contains(worker)) {
            throw new IllegalArgumentException("Worker already exists");
        }

        service.getWorkers().add(worker);
        serviceRepository.save(service);
    }

    @Transactional
    public void removeWorkerFromService(UUID serviceId, UUID workerId) {
        ServiceEntity service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));

        Worker worker = workerRepository.findById(workerId)
                .orElseThrow(() -> new RuntimeException("Worker not found"));

        if (!service.getWorkers().contains(worker)) {
            throw new RuntimeException("Worker not assigned to this service");
        }

        service.getWorkers().remove(worker);
        serviceRepository.save(service);
    }


    public void deleteServiceById(UUID serviceId) {
        log.info("Start deleting service with id: {}", serviceId);
        serviceRepository.deleteById(serviceId);
        log.info("Successfully deleted service with id: {}", serviceId);
    }


//    public Page<ServiceResponse> searchServices(String category, Pageable pageable) {
//        Page<ServiceEntity> services;
//
//        if (category != null && !category.isBlank()) {
//            services = serviceRepository.findByCategoryAndIsActiveTrue(Category.valueOf(category), pageable);
//        } else {
//            services = serviceRepository.findByIsActiveTrue(pageable);
//        }
//
//        return services.map(this::mapToResponse);
//    }


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

        Page<ServiceEntity> services = serviceRepository.searchByFilters(categoryEnum, minPrice, maxPrice, pageable);
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

    public List<ServiceSummaryResponse> getPublicServicesByWorker(UUID workerId) {
        return serviceRepository.findByWorkerIdAndIsActiveTrue(workerId)
                .stream()
                .map(serviceMapper::toSummaryResponse)
                .toList();
    }

    @Transactional
    public ServiceEntity updateImage(UUID serviceId, String url, UUID actorId) {
        ServiceEntity s = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found"));
        var actor = userRepository.findById(actorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Actor not found"));

        var providerOwnerId = s.getProvider() != null && s.getProvider().getOwner() != null
                ? s.getProvider().getOwner().getId() : null;

        boolean isOwner = providerOwnerId != null && providerOwnerId.equals(actorId);
        boolean isAdmin = actor.getRole() != null && actor.getRole().name().equals("ADMIN");
        if (!isOwner && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to update service image");
        }

        s.setImageUrl(url);
        return serviceRepository.save(s);
    }

}
