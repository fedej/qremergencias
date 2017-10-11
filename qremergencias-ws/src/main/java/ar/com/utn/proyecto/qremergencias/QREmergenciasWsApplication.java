package ar.com.utn.proyecto.qremergencias;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = "ar.com.utn.proyecto.qremergencias")
@EnableCaching
@EnableAsync
@EntityScan(basePackages = "ar.com.utn.proyecto.qremergencias")
@SuppressWarnings("PMD.UseUtilityClass")
public class QREmergenciasWsApplication {

    public static void main(final String... args) {
        SpringApplication.run(QREmergenciasWsApplication.class, args);

        /*
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(1024);
        KeyPair pair = keyGen.generateKeyPair();
        PrivateKey privateKey = pair.getPrivate();
        PublicKey publicKey = pair.getPublic();

        MobileTestController bean = run.getBean(MobileTestController.class);
        UserFrontRepository bean1 = run.getBean(UserFrontRepository.class);
        UserFront byUsername = bean1.findByUsername("paciente@rrramundo.com.ar");
        MobileTestController.PublicKeyDTO body1 = new MobileTestController.PublicKeyDTO();
        body1.setPublicKey(Base64.getEncoder().encodeToString(publicKey.getEncoded()));
        bean.uploadPublicKey(body1, byUsername);

        MobileTestController.VerificationDTO body = new MobileTestController.VerificationDTO();
        body.setMessage(byUsername.getId());

        final Signature dsa = Signature.getInstance("SHA256withRSA");
        dsa.initSign(privateKey);
        dsa.update(byUsername.getId().getBytes());
        body.setSignature(Base64.getEncoder().encodeToString(dsa.sign()));
        boolean b = bean.verifySignature(body);
        System.out.println(b);

        final MobileTestController.VerificationDTO body2 = new MobileTestController.VerificationDTO();
        body2.setMessage("SARASA");
        dsa.initSign(privateKey);
        dsa.update("SARASA".getBytes());
        body2.setSignature(Base64.getEncoder().encodeToString(dsa.sign()));
        boolean b2 = bean.verifySignature(body2);
        System.out.println(b2);
        */

    }


}
