package uz.navbatuz.backend.provider.dto;

import uz.navbatuz.backend.common.Category;
import uz.navbatuz.backend.location.dto.LocationSummary;

public record ProviderResponse(
        java.util.UUID id,
        String name,
        String description,
        float avgRating,
        Category category,
        LocationSummary location // may be null
) {}