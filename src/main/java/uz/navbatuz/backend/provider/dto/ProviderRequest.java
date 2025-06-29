package uz.navbatuz.backend.provider.dto;
import jakarta.validation.constraints.*;
import lombok.Data;


@Data
public class ProviderRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @NotBlank
    private String category;

    @NotNull
    private int teamSize;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?998\\d{9}$", message = "Phone must be valid Uzbekistan number")
    private String phoneNumber;

}
