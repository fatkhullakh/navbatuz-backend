package uz.navbatuz.backend.location.dto;

import java.util.UUID;

public record LocationSummary(
        UUID id,
        String addressLine1,
        String city,
        String countryIso2
) {
}
