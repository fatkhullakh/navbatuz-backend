package uz.navbatuz.backend.customer.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CustomerLocationPayload {
    @NotNull @DecimalMin(value = "-90")  @DecimalMax(value = "90")
    private Double lat;
    @NotNull @DecimalMin(value = "-180") @DecimalMax(value = "180")
    private Double lon;

    @Pattern(regexp = "^[A-Z]{2}$", message = "countryIso2 must be ISO-2 uppercase")
    private String countryIso2; // optional; fallback to RegisterRequest.country

    private String city;     // EN label preferred
    private String district; // EN label, optional
}
