package uz.navbatuz.backend.customer.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record CustomerLocationRequest(
        @NotNull @DecimalMin(value="-90") @DecimalMax(value="90")  Double lat,
        @NotNull @DecimalMin(value="-180") @DecimalMax(value="180") Double lon,
        @Pattern(regexp = "^[A-Z]{2}$") String countryIso2, // optional
        String city,                                        // optional
        String district                                      // optional
) {}
