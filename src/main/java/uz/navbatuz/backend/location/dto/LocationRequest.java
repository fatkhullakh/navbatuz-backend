package uz.navbatuz.backend.location.dto;

// request -> from client to API
public record LocationRequest(
        String addressLine1,
        String addressLine2,
        String district,
        String city,
        String countryIso2,   // e.g. "UZ"
        String postalCode,
        Double latitude,      // required
        Double longitude,     // required
        String provider,      // optional (e.g., "osm", "google")
        String providerPlaceId
) {}


