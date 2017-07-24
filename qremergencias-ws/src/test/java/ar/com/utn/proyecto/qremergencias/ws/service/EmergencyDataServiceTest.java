package ar.com.utn.proyecto.qremergencias.ws.service;

import ar.com.utn.proyecto.qremergencias.core.domain.UserEmergencyContact;
import ar.com.utn.proyecto.qremergencias.core.repository.UserEmergencyContactRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

public class EmergencyDataServiceTest {

    public static final String ID_CONTACT = "123123123";
    @InjectMocks
    private final EmergencyDataService service = new EmergencyDataService();

    @Mock
    private UserEmergencyContactRepository userEmergencyContactRepository;

    private UserEmergencyContact contactForTest;

    @Before
    public void setUp() throws Exception {

        MockitoAnnotations.initMocks(this);
        contactForTest = new UserEmergencyContact();
        contactForTest.setFirstName("Gonzalo");
        contactForTest.setLastName("RRRamundo");
        contactForTest.setId(ID_CONTACT);
        contactForTest.setPhoneNumber("1514565543");
    }

    @Test
    public void testFindContact() {
        when(userEmergencyContactRepository.findOne(ID_CONTACT)).thenReturn(contactForTest);
        final UserEmergencyContact contactFound = service.findContact(ID_CONTACT);
        assertNotNull(contactFound);
    }

    @Test
    public void testContactNotFound() {
        when(userEmergencyContactRepository.findOne(ID_CONTACT)).thenReturn(null);
        final UserEmergencyContact contact = service.findContact(ID_CONTACT);
        assertNull(contact);
    }

    

}
