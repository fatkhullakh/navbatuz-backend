package uz.navbatuz.backend.service.dto;

import uz.navbatuz.backend.common.Category;

import java.math.BigDecimal;
import java.util.UUID;

public record ServiceSummaryResponse(
        UUID id,
        String name,
        Category category,
        BigDecimal price,
        Integer duration
) {}
