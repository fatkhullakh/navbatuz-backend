package uz.navbatuz.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.uploads")
public class FileStorageProperties {
    /**
     * Directory on disk where files are stored.
     * e.g. "uploads" or "/var/app/uploads"
     */
    private String dir = "uploads";

    /**
     * Base URL used to build public URLs.
     * e.g. "http://localhost:8080"
     */
    private String baseUrl = "http://localhost:8080";
}
