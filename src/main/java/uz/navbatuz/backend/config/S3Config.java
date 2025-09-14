// uz/navbatuz/backend/config/S3Config.java
package uz.navbatuz.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {
    @Bean
    public S3Client s3Client(FileStorageProperties props) {
        String region = (props.getS3Region() == null || props.getS3Region().isBlank())
                ? "eu-central-1" : props.getS3Region();
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}
