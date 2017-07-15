package ar.com.utn.proyecto.qremergencias.ws.service;

import ar.com.utn.proyecto.qremergencias.core.domain.UserFront;
import ar.com.utn.proyecto.qremergencias.core.repository.UserFrontRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@SuppressWarnings("PMD.JUnitTestContainsTooManyAsserts")
public class UserFrontServiceTest {

    @InjectMocks
    private final UserFrontService service = new UserFrontService();

    @Mock
    private UserFrontRepository userFrontRepository;

    @Mock
    private UserFront userMock;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(userMock.getUsername()).thenReturn("TheUserName");
        when(userMock.getName()).thenReturn("nombre");
        when(userMock.getLastname()).thenReturn("apellido");
        when(userMock.getEmail()).thenReturn("nombre.apellido@gl.com.tar");
    }

    @Test
    public void testFindByUsernameString() {
        when(userFrontRepository.findByUsername(anyString())).thenReturn(userMock);
        final UserFront user = service.findByUsername(userMock.getUsername());
        assertNotNull(user);
        assertEquals(user.getUsername(), userMock.getUsername());
    }
}
