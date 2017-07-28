package ar.com.utn.proyecto.qremergencias.ws.controller;

import ar.com.utn.proyecto.qremergencias.QREmergenciasWsApplication;
import ar.com.utn.proyecto.qremergencias.core.domain.MedicalRecord;
import ar.com.utn.proyecto.qremergencias.core.domain.UserFront;
import ar.com.utn.proyecto.qremergencias.core.dto.MedicalRecordDTO;
import ar.com.utn.proyecto.qremergencias.ws.service.MedicalRecordService;
import ar.com.utn.proyecto.qremergencias.ws.service.UserFrontService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.Filter;
import java.nio.charset.Charset;
import java.time.LocalDate;

import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = QREmergenciasWsApplication.class)
@ActiveProfiles("test")
public class MedicalRecordControllerIT {

    private static final String REPORT_TEXT = "Informe del estudio";
    private static final String STUDY_NAME = "Estudio";

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private Filter springSecurityFilterChain;

    @Autowired
    private UserFrontService userFrontService;

    @Autowired
    private MedicalRecordService medicalRecordService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserFront paciente;
    private UserFront medico;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        paciente = userFrontService.findByUsername("user@rrramundo.com.ar");
        medico = userFrontService.findByUsername("medico@rrramundo.com.ar");
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(this.wac)
                .addFilters(springSecurityFilterChain)
                .build();
    }

    @Test
    public void testGetMedicalRecords() {
        try {
            this.mockMvc
                    .perform(get("/api/medicalRecord")
                            .with(user(paciente))
                            .accept(MediaType.APPLICATION_JSON_UTF8_VALUE))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                    .andExpect(jsonPath("$.totalElements").value(not(0)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testMedicoCanCreateMedicalRecord() {
        try {

            final MockMultipartFile file = new MockMultipartFile("files", "file.txt",
                    "text/plain", "Estoy vacio".getBytes(Charset.defaultCharset()));

            this.mockMvc
                    .perform(fileUpload("/api/medicalRecord")
                            .file(file)
                            .param("name", STUDY_NAME)
                            .param("text", REPORT_TEXT)
                            .param("performed", LocalDate.now().toString())
                            .param("user", paciente.getId())
                            .with(user(medico))
                            .contentType(MediaType.MULTIPART_FORM_DATA)
                            .accept(MediaType.APPLICATION_JSON_UTF8_VALUE))
                    .andExpect(status().isCreated());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testPacienteCannotCreateMedicalRecord() {
        try {

            final MedicalRecordDTO medicalRecordDTO = new MedicalRecordDTO(STUDY_NAME, REPORT_TEXT,
                    LocalDate.now(), paciente.getId());

            this.mockMvc
                    .perform(post("/api/medicalRecord")
                            .content(objectMapper.writeValueAsString(medicalRecordDTO))
                            .with(user(paciente))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON_UTF8_VALUE))
                    .andExpect(status().is(not(lessThanOrEqualTo(299))));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testMedicoCanDeleteMedicalRecord() {
        try {
            final MedicalRecordDTO medicalRecordDTO = new MedicalRecordDTO(STUDY_NAME, REPORT_TEXT,
                    LocalDate.now(), paciente.getId());
            final MedicalRecord save = medicalRecordService.save(medico, medicalRecordDTO, null);

            this.mockMvc
                    .perform(delete("/api/medicalRecord/{id}", save.getId())
                            .with(user(medico))
                            .accept(MediaType.APPLICATION_JSON_UTF8_VALUE))
                    .andExpect(status().isNoContent());

            Assert.assertTrue(medicalRecordService.findById(save.getId()).isDeleted());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
