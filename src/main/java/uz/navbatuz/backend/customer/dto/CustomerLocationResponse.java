package uz.navbatuz.backend.customer.dto;

public record CustomerLocationResponse(
        Double lat, Double lon,
        String countryIso2, String city, String district
) {}