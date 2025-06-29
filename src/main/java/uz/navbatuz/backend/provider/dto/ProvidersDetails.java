package uz.navbatuz.backend.provider.dto;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProvidersDetails {
    private String name;
    private String description;
    private String category;
    private int teamSize;
    private String email;
    private String phone;
    private float avgRating;
}
