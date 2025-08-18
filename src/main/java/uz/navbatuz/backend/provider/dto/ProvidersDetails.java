package uz.navbatuz.backend.provider.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import uz.navbatuz.backend.common.Category;
import uz.navbatuz.backend.location.dto.LocationSummary;
import uz.navbatuz.backend.provider.model.BusinessHour;
import uz.navbatuz.backend.worker.dto.WorkerResponseForService;

import java.util.List;
import java.util.UUID;

public record ProvidersDetails(
        UUID id,
     String name,
     String description,
     Category category,
     List<WorkerResponseForService> workers,
     String email,
     String phone,
        String logoUrl,
     float avgRating,
     List<BusinessHourResponse> businessHours,
        LocationSummary location
) {
}
