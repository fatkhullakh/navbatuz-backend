package uz.navbatuz.backend.location.dto;

// response -> from API to client
import java.time.OffsetDateTime;
import java.util.UUID;

public record LocationResponse(
        UUID id,
        String addressLine1,
        String addressLine2,
        String district,
        String city,
        String countryIso2,
        String postalCode,
        Double latitude,
        Double longitude,
        boolean active,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}

