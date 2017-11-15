package ar.com.utn.proyecto.qremergencias;

import ar.com.utn.proyecto.qremergencias.core.domain.UserFront;
import ar.com.utn.proyecto.qremergencias.core.domain.UserVerificationToken;
import ar.com.utn.proyecto.qremergencias.core.dto.ConfirmRegistrationDTO;
import ar.com.utn.proyecto.qremergencias.core.dto.CreateUserDTO;
import ar.com.utn.proyecto.qremergencias.core.dto.UserContactDTO;
import ar.com.utn.proyecto.qremergencias.core.dto.UserProfileDTO;
import ar.com.utn.proyecto.qremergencias.core.repository.UserFrontRepository;
import ar.com.utn.proyecto.qremergencias.ws.controller.EmergencyDataController;
import ar.com.utn.proyecto.qremergencias.ws.controller.ProfileController;
import ar.com.utn.proyecto.qremergencias.ws.controller.UserFrontController;
import ar.com.utn.proyecto.qremergencias.ws.service.UserFrontService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;

@SpringBootApplication(scanBasePackages = "ar.com.utn.proyecto.qremergencias")
@EnableCaching
@EnableAsync
@EntityScan(basePackages = "ar.com.utn.proyecto.qremergencias")
@SuppressWarnings("PMD")
public class QREmergenciasWsApplication {

    public static void main(final String... args) throws IOException {
        ConfigurableApplicationContext run = SpringApplication.run(QREmergenciasWsApplication.class, args);

        final RandomNameGenerator randomNameGenerator = new RandomNameGenerator();
        final UserFrontController userFrontController = run.getBean(UserFrontController.class);
        final UserFrontRepository userFrontRepository = run.getBean(UserFrontRepository.class);
        final UserFrontService userService = run.getBean(UserFrontService.class);
        final ProfileController profileController = run.getBean(ProfileController.class);
        final EmergencyDataController emergencyDataController = run.getBean(EmergencyDataController.class);

        final Random random = new Random();
        for (int i = 0; i < 10; i++) {
            final CreateUserDTO model = new CreateUserDTO();
            final String name = randomNameGenerator.nextName();
            final String lastname = randomNameGenerator.nextLastName();
            String email = (name.toLowerCase() + "." + lastname.toLowerCase() + "@example.com").replace(" ", "_");
            model.setEmail(email);
            model.setPassword("Passw0rd!");
            model.setRole("ROLE_PACIENTE");
            userFrontController.register(model);

            final UserFront user = userFrontRepository.findByUsername(email);
            if (user != null) {
                UserVerificationToken userVerificationByUser = userService.getUserVerificationByUser(user);
                ConfirmRegistrationDTO request = new ConfirmRegistrationDTO();
                request.setBirthDate(LocalDate.now());
                request.setIdNumber(String.valueOf((int) (100000 + random.nextDouble() * 900000)));
                request.setToken(userVerificationByUser.getToken());
                request.setLastName(lastname);
                request.setName(name);
                request.setSex('M');
                userFrontController.completeRegistration(request, null);

                SecurityContextHolder.getContext()
                        .setAuthentication(new UsernamePasswordAuthenticationToken(user, null,
                                user.getAuthorities()));
                final UserProfileDTO userProfileDTO = profileController.list(user);
                final UserContactDTO userContactDTO = new UserContactDTO();
                userContactDTO.setFirstName(randomNameGenerator.nextName());
                userContactDTO.setLastName(randomNameGenerator.nextLastName());
                userContactDTO.setPrimary(true);
                userContactDTO.setPhoneNumber(String.valueOf((int) (100000 + random.nextDouble() * 900000)));
                if (userProfileDTO.getContacts() == null) {
                    userProfileDTO.setContacts(new ArrayList<>());
                }
                userProfileDTO.getContacts().add(userContactDTO);
                profileController.update(userProfileDTO, user, false);
                emergencyDataController.createQR(user);
            } else {
                System.out.println("Usuario no creado");
            }

        }

    }

    @PostConstruct
    public void configureTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC));
    }

    public static class RandomNameGenerator {
        private int pos;

        public RandomNameGenerator(int seed) {
            this.pos = seed;
        }

        public RandomNameGenerator() {
            this((int) System.currentTimeMillis());
        }

        public synchronized String nextName() {
            Dictionary d = Dictionary.INSTANCE;
            pos = Math.abs(pos + d.getPrime()) % d.size();
            return d.name(pos);
        }

        public synchronized String nextLastName() {
            Dictionary d = Dictionary.INSTANCE;
            pos = Math.abs(pos + d.getPrime()) % d.size();
            return d.lastname(pos);
        }
    }

    public static class Dictionary {
        private List<String> nouns = new ArrayList<>();
        private List<String> adjectives = new ArrayList<>();

        private final int prime;

        public Dictionary() {
            try {
                load("names.txt", adjectives);
                load("lastnames.txt", nouns);
            } catch (IOException e) {
                throw new Error(e);
            }

            int combo = size();

            int primeCombo = 2;
            while (primeCombo <= combo) {
                int nextPrime = primeCombo + 1;
                primeCombo *= nextPrime;
            }
            prime = primeCombo + 1;
        }

        public int size() {
            return nouns.size() * adjectives.size();
        }

        public int getPrime() {
            return prime;
        }

        public String name(int i) {
            int a = i % adjectives.size();
            return adjectives.get(a);
        }

        public String lastname(int i) {
            int n = i / adjectives.size();
            return nouns.get(n);
        }

        private void load(String name, List<String> col) throws IOException {
            Path path = Paths.get(System.getProperty("user.dir"), "qremergencias-ws/src/main/resources", name);
            List<String> strings = Files.readAllLines(path);
            col.addAll(strings);
        }

        static final Dictionary INSTANCE = new Dictionary();
    }


}
