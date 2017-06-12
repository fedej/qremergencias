package ar.com.utn.proyecto.qremergencias.bo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = "ar.com.utn.proyecto.qremergencias")
@EnableCaching
@EnableAsync
@EntityScan(basePackages = "ar.com.utn.proyecto.qremergencias")
@EnableMongoRepositories(basePackages = "ar.com.utn.proyecto.qremergencias")
@SuppressWarnings("PMD.UseUtilityClass")
public class QREmergenciasApplication {

    public static void main(final String... args) {
        SpringApplication.run(QREmergenciasApplication.class, args);
    }
}
