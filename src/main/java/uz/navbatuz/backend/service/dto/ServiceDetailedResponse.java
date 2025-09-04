package uz.navbatuz.backend.service.dto;

import uz.navbatuz.backend.common.Category;
import uz.navbatuz.backend.worker.dto.WorkerResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ServiceDetailedResponse(
        UUID id,
        String name,
        String description,
        Category category,
        BigDecimal price,
        Integer duration,
        boolean isActive,
        UUID providerId,
        List<WorkerResponse> workers,
        String logoUrl
) {}
