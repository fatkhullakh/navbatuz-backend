package uz.navbatuz.backend.service.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uz.navbatuz.backend.provider.model.Provider;
import uz.navbatuz.backend.provider.repository.ProviderRepository;
import uz.navbatuz.backend.service.dto.CreateServiceRequest;
import uz.navbatuz.backend.service.dto.ServiceResponse;
import uz.navbatuz.backend.service.model.ServiceEntity;
import uz.navbatuz.backend.service.repository.ServiceRepository;
import uz.navbatuz.backend.worker.model.Worker;
import uz.navbatuz.backend.worker.repository.WorkerRepository;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceService {
    private final ServiceRepository serviceRepository;
    private final ProviderRepository providerRepository;
    private final WorkerRepository workerRepository;

    public List<ServiceEntity> getAllActiveServicesByProvider (UUID providerId) {
        return serviceRepository.findByProviderIdAndIsActiveTrue(providerId);
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

    private ServiceResponse mapToResponse(ServiceEntity service) {
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


}
