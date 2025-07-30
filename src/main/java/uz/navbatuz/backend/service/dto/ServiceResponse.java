package uz.navbatuz.backend.service.dto;

import uz.navbatuz.backend.common.Category;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

public record ServiceResponse(
        UUID id,
        String name,
        String description,
        Category category,
        BigDecimal price,
        Duration duration,
        boolean isActive,
        UUID providerId,
        List<UUID> workerIds
) {}

