package ar.com.utn.proyecto.qremergencias.ws.service;

import ar.com.utn.proyecto.qremergencias.core.domain.UserEmergencyContact;
import ar.com.utn.proyecto.qremergencias.core.repository.UserEmergencyContactRepository;
import ar.com.utn.proyecto.qremergencias.core.repository.UserFrontRepository;
import ar.com.utn.proyecto.qremergencias.core.repository.UserRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class EmergencyDataServiceTest {

    @InjectMocks
    private final EmergencyDataService service = new EmergencyDataService();

    @Mock
    private UserFrontRepository userFrontRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserEmergencyContactRepository userEmergencyContactRepository;

    UserEmergencyContact contactForTest;

    @Before
    public void setUp() throws Exception {

        MockitoAnnotations.initMocks(this);
        contactForTest = new UserEmergencyContact();
        contactForTest.setFirstName("Gonzalo");
        contactForTest.setLastName("RRRamundo");
        contactForTest.setId("123131231");
        contactForTest.setPhoneNumber("1514565543");
    }

    @Test
    public void testFindContact() {
        when(userEmergencyContactRepository.findOne("123131231")).thenReturn(contactForTest);
        UserEmergencyContact contactFound = service.findContact("123131231");
        assertNotNull(contactFound);
        assertEquals("Gonzalo", contactFound.getFirstName());
        assertEquals("RRRamundo", contactFound.getLastName());
        assertNotEquals("1566776655", contactFound.getPhoneNumber());
    }

    @Test
    public void testContactNotFound() {
        when(userEmergencyContactRepository.findOne("123131231")).thenReturn(null);
        UserEmergencyContact contact = service.findContact("123131231");
        assertNull(contact);
    }

    

}
