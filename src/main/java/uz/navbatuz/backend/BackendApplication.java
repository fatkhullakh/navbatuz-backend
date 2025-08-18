package uz.navbatuz.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import uz.navbatuz.backend.config.FileStorageProperties;

@SpringBootApplication
@EnableConfigurationProperties(FileStorageProperties.class)
public class BackendApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure()
				.directory(System.getProperty("user.dir")) // absolute path to working dir
				.filename(".env")
				.ignoreIfMalformed()
				.ignoreIfMissing()
				.load();

		System.setProperty("DATABASE_URL", dotenv.get("DATABASE_URL"));
		System.setProperty("DATABASE_USERNAME", dotenv.get("DATABASE_USERNAME"));
		System.setProperty("DATABASE_PASSWORD", dotenv.get("DATABASE_PASSWORD"));
		System.setProperty("JWT_SECRET_KEY", dotenv.get("JWT_SECRET_KEY"));

		SpringApplication.run(BackendApplication.class, args);

	}


}
