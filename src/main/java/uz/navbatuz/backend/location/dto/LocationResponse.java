package uz.navbatuz.backend.location.dto;

import java.util.UUID;

public record LocationResponse(
        UUID id,
        String address,
        String district,
        String city,
        String country,
        String postalCode,
        Double latitude,
        Double longitude
) {}
