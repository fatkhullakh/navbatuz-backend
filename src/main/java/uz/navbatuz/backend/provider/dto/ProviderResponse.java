package uz.navbatuz.backend.provider.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import uz.navbatuz.backend.location.model.Location;

@Data
@AllArgsConstructor
public class ProviderResponse {
    private String name;
    private String description;
    private float avgRating;
    private Location location;
}
