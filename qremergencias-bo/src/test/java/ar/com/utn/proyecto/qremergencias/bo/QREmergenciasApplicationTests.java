package ar.com.utn.proyecto.qremergencias.bo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = QREmergenciasApplication.class)
@TestPropertySource("classpath:application-test.properties")
public class QREmergenciasApplicationTests {

    @Test
    public void contextLoads() {
    }

}
