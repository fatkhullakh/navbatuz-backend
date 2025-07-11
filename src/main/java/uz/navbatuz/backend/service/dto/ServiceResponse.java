package uz.navbatuz.backend.service.dto;

import uz.navbatuz.backend.common.Category;

import java.math.BigDecimal;
import java.util.UUID;

public record ServiceResponse(
        UUID id,
        String name,
        String description,
        Category category,
        BigDecimal price,
        Integer duration,
        Boolean isActive,
        UUID providerId,
        UUID workerId
        //List<String> workerNames
) {}

