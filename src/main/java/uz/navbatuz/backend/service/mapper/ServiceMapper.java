package uz.navbatuz.backend.service.mapper;

import org.springframework.stereotype.Component;
import uz.navbatuz.backend.service.dto.ServiceResponse;
import uz.navbatuz.backend.service.dto.ServiceSummaryResponse;
import uz.navbatuz.backend.service.model.ServiceEntity;
import uz.navbatuz.backend.worker.model.Worker;

@Component
public class ServiceMapper {

    public ServiceSummaryResponse toSummaryResponse(ServiceEntity service) {
        return new ServiceSummaryResponse(
                service.getId(),
                service.getName(),
                service.getCategory(),
                service.getPrice(),
                service.getDuration()
        );
    }

    public ServiceResponse toDetailedResponse(ServiceEntity service) {
        return new ServiceResponse(
                service.getId(),
                service.getName(),
                service.getDescription(),
                service.getCategory(),
                service.getPrice(),
                service.getDuration(),
                service.isActive(),
                service.getProvider().getId(),
                service.getWorkers().stream().map(Worker::getId).toList()
        );
    }
}
