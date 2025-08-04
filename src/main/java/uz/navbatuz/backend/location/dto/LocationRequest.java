package uz.navbatuz.backend.location.dto;

public record LocationRequest(
        String address,
        String district,
        String city,
        String country,
        String postalCode,
        Double latitude,
        Double longitude
) {}

