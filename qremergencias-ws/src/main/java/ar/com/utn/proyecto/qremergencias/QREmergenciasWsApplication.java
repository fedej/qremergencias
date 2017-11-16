package ar.com.utn.proyecto.qremergencias;

import ar.com.utn.proyecto.qremergencias.core.domain.DoctorFront;
import ar.com.utn.proyecto.qremergencias.core.domain.UserFront;
import ar.com.utn.proyecto.qremergencias.core.domain.UserVerificationToken;
import ar.com.utn.proyecto.qremergencias.core.dto.ConfirmRegistrationDTO;
import ar.com.utn.proyecto.qremergencias.core.dto.CreateUserDTO;
import ar.com.utn.proyecto.qremergencias.core.dto.UserContactDTO;
import ar.com.utn.proyecto.qremergencias.core.dto.UserProfileDTO;
import ar.com.utn.proyecto.qremergencias.core.dto.emergency.EmergencyDataDTO;
import ar.com.utn.proyecto.qremergencias.core.dto.emergency.GeneralDataDTO;
import ar.com.utn.proyecto.qremergencias.core.dto.emergency.HospitalizationDTO;
import ar.com.utn.proyecto.qremergencias.core.dto.emergency.MedicationDTO;
import ar.com.utn.proyecto.qremergencias.core.dto.emergency.PathologyDTO;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

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
        // TODO generarMedicos();
        generarPacientes(randomNameGenerator, userFrontController, userFrontRepository, userService, profileController,
                emergencyDataController, random);

    }

    private static void generarPacientes(RandomNameGenerator randomNameGenerator,
                                         UserFrontController userFrontController,
                                         UserFrontRepository userFrontRepository,
                                         UserFrontService userService,
                                         ProfileController profileController,
                                         EmergencyDataController emergencyDataController, Random random)
            throws IOException {
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
                request.setBirthDate(randomDate(LocalDate.of(1970, 1, 1), LocalDate.of(1998, 12, 12)));
                request.setIdNumber(String.valueOf((int) (100000 + random.nextDouble() * 900000)));
                request.setToken(userVerificationByUser.getToken());
                request.setLastName(lastname);
                request.setName(name);
                request.setSex(randomSex());
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

                UserFront doctorFront = randomDoctor(userFrontRepository);
                SecurityContextHolder.getContext()
                        .setAuthentication(new UsernamePasswordAuthenticationToken(doctorFront, null,
                                doctorFront.getAuthorities()));
                emergencyDataController.updateEmergencyData(randomEmergencyDataDTO(user), user.getUsername(), false);

                SecurityContextHolder.getContext()
                        .setAuthentication(new UsernamePasswordAuthenticationToken(user, null,
                                user.getAuthorities()));
                emergencyDataController.createQR(user);
            } else {
                System.out.println("Usuario no creado");
            }

        }
    }

    private static EmergencyDataDTO randomEmergencyDataDTO(final UserFront userFront) {
        EmergencyDataDTO emergencyDataDTO = new EmergencyDataDTO();
        emergencyDataDTO.setUuid(UUID.randomUUID().toString());
        emergencyDataDTO.setSurgeries(randomSurgeries(userFront));
        emergencyDataDTO.setPathologies(randomPathologies(userFront));
        emergencyDataDTO.setMedications(randomMedications());
        emergencyDataDTO.setHospitalizations(randomHospitalizations(userFront));
        emergencyDataDTO.setGeneral(randomGeneralDataDTO(userFront));
        return emergencyDataDTO;
    }

    private static final List<MedicationDTO> medications =
            Arrays.asList(new MedicationDTO("Ibuprofeno", "600mg", 3,
                            MedicationDTO.Period.diariamente),
                    new MedicationDTO("Valium", "70mg", 10, MedicationDTO.Period.semanalmente),
                    new MedicationDTO("Cortisona", "5mg", 4, MedicationDTO.Period.mensualmente),
                    new MedicationDTO("Cinarizina", "75mg", 2, MedicationDTO.Period.diariamente)
            );

    private static List<MedicationDTO> randomMedications() {
        final Random random = new Random();
        int surgeries = random.nextInt(4) + 1;
        Collections.shuffle(medications);
        final ArrayDeque<MedicationDTO> medicationsa = new ArrayDeque<>(medications);
        final List<MedicationDTO> medicationsDTOs = new ArrayList<>(surgeries);
        for (int i = 0; i < surgeries; i++) {
            medicationsDTOs.add(medicationsa.pop());
        }
        return medicationsDTOs;
    }

    private static final List<String> patos = Arrays.asList("Acalasia Idiopática", "Neumonía", "Artritis", "Hemofilia",
            "Hepatitis C", "hipertension", "asma", "antecedentes_oncologicos", "insuficiencia_suprarrenal");

    private static List<PathologyDTO> randomPathologies(final UserFront userFront) {
        final Random random = new Random();
        int cantidad = random.nextInt(5) + 1;
        Collections.shuffle(patos);
        final ArrayDeque<String> desctiptions = new ArrayDeque<>(patos);
        ArrayList<PathologyDTO> pathologyDTOs = new ArrayList<>();
        for (int i = 0; i < cantidad; i++) {
            PathologyDTO pathologyDTO = new PathologyDTO();
            pathologyDTO.setDate(randomDate(userFront.getBirthdate(), LocalDate.now()));

            String pato = desctiptions.pop();
            if (pato.equals(pato.toLowerCase())) {
                pathologyDTO.setDescription(null);
                pathologyDTO.setType(PathologyDTO.Type.valueOf(pato));
            } else {
                pathologyDTO.setDescription(pato);
                pathologyDTO.setType(PathologyDTO.Type.otro);
            }
            pathologyDTOs.add(pathologyDTO);
        }

        return pathologyDTOs;
    }

    private static final List<String> cirugias = Arrays.asList("Yeyunostomía", "Biopsia rectal",
            "Cirugía del reflujo gastroesofágico", "Traqueostomía", "Traqueoplastia", "Corrección de cicatrices",
            "Endarterectomías", "Criocirugía de piel", "Evisceración",
            "Pterigion", "Anillos intraestromales", "Catarata", "Compartitectomías", "Osteotomías",
            "Elongaciones óseas");

    private static final List<String> instituciones = Arrays.asList("Clinica San Camilo", "Hospital Naval",
            "Hospital Pirovano", "Sanatorio de la Trinidad", "Hospital Britanico", "Hospital Aleman",
            "Hospital Fernandez",
            "Hospital General de Agudos", "Hospital Italiano", "Hospital Tornú",
            "Hospital Ramos Mejía", "Hospital Piñero", "Hospital de Niños Ricardo Gutierrez", "Clinica de la Esperanza",
            "Hospital Sirio Libanes");

    private static final List<String> alergias = Arrays.asList("Ácaros", "Polen", "Picadura de abejas", "penicilina",
            "insulina", "rayos_x_con_yodo", "sulfamidas");

    private static final List<String> admisiones = Arrays.asList("Cuadro viral", "Lipotimia",
            "Deshidratacion", "Coma", "Politraumatismo");

    private static LocalDate randomDate(LocalDate desde, LocalDate hasta) {
        long minDay = desde.toEpochDay();
        long maxDay = hasta.toEpochDay();
        long randomDay = ThreadLocalRandom.current().nextLong(minDay, maxDay);
        return LocalDate.ofEpochDay(randomDay);
    }

    private static List<HospitalizationDTO> randomSurgeries(final UserFront userFront) {
        final Random random = new Random();
        int surgeries = random.nextInt(15) + 1;
        Collections.shuffle(cirugias);
        Collections.shuffle(instituciones);
        final ArrayDeque<String> reasons = new ArrayDeque<>(cirugias);
        final ArrayDeque<String> instituitions = new ArrayDeque<>(instituciones);
        final List<HospitalizationDTO> hospitalizationDTOs = new ArrayList<>(surgeries);
        for (int i = 0; i < surgeries; i++) {
            final HospitalizationDTO hospitalizationDTO = new HospitalizationDTO();
            hospitalizationDTO.setDate(randomDate(userFront.getBirthdate(), LocalDate.now()));
            hospitalizationDTO.setInstitution(instituitions.pop());
            hospitalizationDTO.setReason(reasons.pop());
            hospitalizationDTO.setType(HospitalizationDTO.Type.cirugia);
            hospitalizationDTOs.add(hospitalizationDTO);
        }
        return hospitalizationDTOs;
    }

    private static List<HospitalizationDTO> randomHospitalizations(final UserFront userFront) {
        final Random random = new Random();
        int surgeries = random.nextInt(5) + 1;
        Collections.shuffle(admisiones);
        Collections.shuffle(instituciones);
        final ArrayDeque<String> reasons = new ArrayDeque<>(admisiones);
        final ArrayDeque<String> instituitions = new ArrayDeque<>(instituciones);
        final List<HospitalizationDTO> hospitalizationDTOs = new ArrayList<>(surgeries);
        for (int i = 0; i < surgeries; i++) {
            final HospitalizationDTO hospitalizationDTO = new HospitalizationDTO();
            hospitalizationDTO.setDate(randomDate(userFront.getBirthdate(), LocalDate.now()));
            hospitalizationDTO.setInstitution(instituitions.pop());
            hospitalizationDTO.setReason(reasons.pop());
            hospitalizationDTO.setType(HospitalizationDTO.Type.admision);
            hospitalizationDTOs.add(hospitalizationDTO);
        }
        return hospitalizationDTOs;
    }

    private static UserFront randomDoctor(UserFrontRepository userFrontRepository) {
        final Random random = new Random();
        Page<DoctorFront> roleMedico = userFrontRepository.findByRolesContaining("ROLE_MEDICO",
                new PageRequest(0, 100));
        return roleMedico.getContent().get(random.nextInt(roleMedico.getNumberOfElements()));
    }

    private static GeneralDataDTO randomGeneralDataDTO(final UserFront userFront) {
        final Random random = new Random();
        GeneralDataDTO generalDataDTO = new GeneralDataDTO();
        generalDataDTO.setLastMedicalCheck(randomDate(userFront.getBirthdate(), LocalDate.now()));
        generalDataDTO.setOrganDonor(random.nextBoolean());
        Collections.shuffle(alergias);
        final ArrayDeque<String> alergies = new ArrayDeque<>(alergias);
        int alergias = random.nextInt(3) + 1;
        final List<String> al = new ArrayList<>(alergias);
        for (int i = 0; i < alergias; i++) {
            al.add(alergies.pop());
        }
        generalDataDTO.setAllergies(al);
        generalDataDTO.setBloodType(randomBlood());

        return generalDataDTO;
    }

    private static String randomBlood() {
        switch (new Random().nextInt(7)) {
            case 0b0000:
                return "0-";
            case 0b0001:
                return "0+";
            case 0b0010:
                return "A-";
            case 0b0011:
                return "A+";
            case 0b0100:
                return "B-";
            case 0b0101:
                return "B+";
            case 0b0110:
                return "AB-";
            default:
                return "AB+";
        }
    }

    private static char randomSex() {
        switch (new Random().nextInt(2)) {
            case 0b00:
                return 'M';
            case 0b01:
                return 'F';
            default:
                return 'O';
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
