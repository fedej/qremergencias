package ar.com.utn.proyecto.qremergencias.ws.controller;

import ar.com.utn.proyecto.qremergencias.QREmergenciasWsApplication;
import ar.com.utn.proyecto.qremergencias.ws.service.UserFrontService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.Filter;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = QREmergenciasWsApplication.class)
public class MedicalRecordControllerIT {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private Filter springSecurityFilterChain;

    @Autowired
    private UserFrontService userFrontService;

    private UserDetails userDetails;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        userDetails = userFrontService.findByUsername("user@rrramundo.com.ar");
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(this.wac)
                .addFilters(springSecurityFilterChain)
                .build();
    }

    @Test
    public void getMedicalRecords() {
        try {
            this.mockMvc
                    .perform(get("/api/medicalRecord")
                            .with(user(userDetails))
                            .accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("application/json;charset=UTF-8"))
                    .andExpect(jsonPath("$.totalElements").value(1));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
