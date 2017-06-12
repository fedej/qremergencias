package ar.com.utn.proyecto.qremergencias;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication(scanBasePackages = "ar.com.utn.proyecto.qremergencias")
@EnableCaching
@EntityScan(basePackages = "ar.com.utn.proyecto.qremergencias")
@SuppressWarnings("PMD.UseUtilityClass")
public class QREmergenciasWsApplication {

    public static void main(final String... args) {
        SpringApplication.run(QREmergenciasWsApplication.class, args);
    }
}
