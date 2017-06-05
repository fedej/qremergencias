package ar.com.utn.proyecto.qremergencias.bo;

import ar.com.utn.proyecto.qremergencias.core.domain.Role;
import ar.com.utn.proyecto.qremergencias.core.domain.User;
import ar.com.utn.proyecto.qremergencias.core.repository.RoleRepository;
import ar.com.utn.proyecto.qremergencias.core.repository.UserRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;

@SpringBootApplication(scanBasePackages = "ar.com.utn.proyecto.qremergencias")
@EnableCaching
@EnableAsync
@EntityScan(basePackages = "ar.com.utn.proyecto.qremergencias")
@SuppressWarnings("PMD.UseUtilityClass")
public class QREmergenciasApplication {

    public static void main(final String... args) {
        final ConfigurableApplicationContext run = SpringApplication.run(QREmergenciasApplication.class, args);

        final RoleRepository roleRepository = run.getBean(RoleRepository.class);
        Role roleAdmin;
        if ((roleAdmin = roleRepository.findByAuthority("ROLE_ADMIN")) == null) {
            roleAdmin = new Role();
            roleAdmin.setAssignable(false);
            roleAdmin.setAuthority("ROLE_ADMIN");
            roleAdmin = roleRepository.save(roleAdmin);
        }

        if (roleRepository.findByAuthority("ROLE_OPERATOR") == null) {
            final Role roleOperator = new Role();
            roleOperator.setAssignable(true);
            roleOperator.setAuthority("ROLE_OPERATOR");
            roleRepository.save(roleOperator);
        }

        final UserRepository userRepository = run.getBean(UserRepository.class);
        final PasswordEncoder pe = run.getBean(PasswordEncoder.class);
        if (userRepository.findByRolesContaining(roleAdmin).isEmpty()) {
            final User user = new User();
            user.setEmail("federico.jaite@gmail.com");
            user.setPassword(pe.encode("admin123"));
            user.setUsername("admin");
            user.setRoles(Collections.singletonList(roleAdmin));
            userRepository.save(user);
        }


    }
}
