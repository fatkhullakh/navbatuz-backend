package uz.navbatuz.backend.provider.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import uz.navbatuz.backend.common.Category;
import uz.navbatuz.backend.location.model.Location;

import java.util.UUID;

@Data
@AllArgsConstructor
public class ProviderResponse {
    private UUID id;
    private String name;
    private String description;
    private float avgRating;
    private Location location;
    private Category category;
}
