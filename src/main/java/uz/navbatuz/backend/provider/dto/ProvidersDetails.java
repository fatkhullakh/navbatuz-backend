package uz.navbatuz.backend.provider.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import uz.navbatuz.backend.common.Category;

@Data
@AllArgsConstructor
public class ProvidersDetails {
    private String name;
    private String description;
    private Category category;
    private int teamSize;
    private String email;
    private String phone;
    private float avgRating;
}
