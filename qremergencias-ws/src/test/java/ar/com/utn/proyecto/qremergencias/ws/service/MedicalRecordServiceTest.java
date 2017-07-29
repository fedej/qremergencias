package ar.com.utn.proyecto.qremergencias.ws.service;

import ar.com.utn.proyecto.qremergencias.core.domain.MedicalRecord;
import ar.com.utn.proyecto.qremergencias.core.domain.UserFront;
import ar.com.utn.proyecto.qremergencias.core.repository.MedicalRecordRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class MedicalRecordServiceTest {

    @InjectMocks
    private final MedicalRecordService service = new MedicalRecordService();

    @Mock
    private MedicalRecordRepository medicalRecordRepository;

    @Mock
    private MedicalRecord mock;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testFindByUsernameString() {
        final List<MedicalRecord> medicalRecordList = Collections.singletonList(mock);
        when(medicalRecordRepository.findByUserAndDeletedIsFalse(any(UserFront.class),
                any(Pageable.class)))
                .thenReturn(new PageImpl<>(medicalRecordList));

        final Page<MedicalRecord> page = service.findByUser(new UserFront(),
                new PageRequest(0, 10));
        assertEquals(mock, page.getContent().get(0));
    }

}
