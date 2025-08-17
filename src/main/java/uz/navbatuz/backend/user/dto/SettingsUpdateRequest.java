package uz.navbatuz.backend.user.dto;

import lombok.Data;
import uz.navbatuz.backend.common.Language;

@Data
public class SettingsUpdateRequest {
    private Language language;   // EN | RU | UZ (nullable)
    private String country;      // ISO2 like "UZ" (nullable)
}
