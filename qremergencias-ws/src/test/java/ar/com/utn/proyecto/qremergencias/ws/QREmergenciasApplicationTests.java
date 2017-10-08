package ar.com.utn.proyecto.qremergencias.ws;

import ar.com.utn.proyecto.qremergencias.QREmergenciasWsApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = QREmergenciasWsApplication.class)
@TestPropertySource("classpath:application-test.properties")
@WebAppConfiguration
public class QREmergenciasApplicationTests {

    @Test
    public void contextLoads() {
    }

}
