// uz/navbatuz/backend/config/FileStorageProperties.java
package uz.navbatuz.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "storage")
public class FileStorageProperties {
    private String dir;
    private String provider;      // "s3" or "disk"
    private String s3Bucket;
    private String s3Region;
    private String publicBaseUrl; // CDN or S3 URL
}
