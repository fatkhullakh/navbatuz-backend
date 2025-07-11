package uz.navbatuz.backend.service.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uz.navbatuz.backend.common.Category;
import uz.navbatuz.backend.provider.model.Provider;
import uz.navbatuz.backend.provider.repository.ProviderRepository;
import uz.navbatuz.backend.service.dto.CreateServiceRequest;
import uz.navbatuz.backend.service.dto.ServiceResponse;
import uz.navbatuz.backend.service.model.ServiceEntity;
import uz.navbatuz.backend.service.repository.ServiceRepository;
import uz.navbatuz.backend.user.model.User;
import uz.navbatuz.backend.worker.dto.WorkerResponse;
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

//    public List<ServiceEntity> getAllActiveServicesByProvider (UUID providerId) {
//        return serviceRepository.findByProviderIdAndIsActiveTrue(providerId);
//    }

    public List<ServiceResponse> getAllActiveServicesByProvider(UUID providerId) {
        return serviceRepository.findByProviderIdAndIsActiveTrue(providerId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<ServiceResponse> getAllServicesByProvider(UUID providerId) {
        return serviceRepository.findByProviderId(providerId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<ServiceResponse> getAllActiveServicesByWorker(UUID workerId) {
        return serviceRepository.findByWorkerIdAndIsActiveTrue(workerId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<ServiceResponse> getAllServicesByWorker(UUID workerId) {
        return serviceRepository.findByWorkerId(workerId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ServiceResponse getService(UUID serviceId) {
        ServiceEntity serviceEntity = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));

        return mapToResponse(serviceEntity);
    }


    public ServiceResponse mapToResponse(ServiceEntity service) {
        return new ServiceResponse(
                service.getId(),
                service.getName(),
                service.getDescription(),
                service.getCategory(),
                service.getPrice(),
                service.getDuration(),
                service.isActive(),
                service.getProvider().getId(),
                service.getWorker().getId()
        );
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

        // For now: just assign the service to the first worker (MVP level)
        Worker worker = workers.get(0);

        ServiceEntity service = ServiceEntity.builder()
                .name(request.name())
                .description(request.description())
                .category(request.category())
                .price(request.price() != null ? java.math.BigDecimal.valueOf(request.price()) : null)
                .duration(request.duration())
                .provider(provider)
                .worker(worker)
                .isActive(true)
                .build();

        service = serviceRepository.save(service);

        return mapToResponse(service); // return DTO
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

        serviceEntity.setName(request.name());
        serviceEntity.setDescription(request.description());
        serviceEntity.setCategory(request.category());
        serviceEntity.setPrice(request.price());
        serviceEntity.setDuration(request.duration());
        serviceEntity.setActive(request.isActive());
        serviceRepository.save(serviceEntity);
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


    public Page<ServiceResponse> searchServices(String category, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
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
        return services.map(this::mapToResponse);
    }



//    private ServiceResponse mapToResponse(ServiceEntity service) {
//        return new ServiceResponse(
//                service.getId(),
//                service.getName(),
//                service.getDescription(),
//                service.getCategory(),
//                service.getPrice(),
//                service.getDuration(),
//                service.isActive(),
//                service.getProvider().getId(),
//                service.getWorker().getId()
//        );
//    }


}
