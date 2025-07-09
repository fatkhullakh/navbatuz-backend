package uz.navbatuz.backend.provider.dto;
import jakarta.validation.constraints.*;
import lombok.Data;
import uz.navbatuz.backend.common.Category;

import java.util.UUID;


@Data
public class ProviderRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @NotNull(message = "Category is required")
    private Category category;

    @NotNull
    private int teamSize;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?998\\d{9}$", message = "Phone must be valid Uzbekistan number")
    private String phoneNumber;

    @NotNull(message = "Owner ID is required")
    private UUID ownerId;

}
