package uz.navbatuz.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import uz.navbatuz.backend.config.FileStorageProperties;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(FileStorageProperties.class)
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

}
