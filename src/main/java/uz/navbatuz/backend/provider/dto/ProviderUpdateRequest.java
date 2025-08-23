package uz.navbatuz.backend.provider.dto;

import uz.navbatuz.backend.common.Category;

public record ProviderUpdateRequest(
        String name,
        String description,
        Category category,
        String email,
        String phoneNumber
) {
}