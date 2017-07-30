package ar.com.utn.proyecto.qremergencias.ws.service;

import ar.com.utn.proyecto.qremergencias.core.domain.UserFront;
import ar.com.utn.proyecto.qremergencias.core.repository.UserFrontRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class UserProfileServiceTest {

    @InjectMocks
    private final UserProfileService service = new UserProfileService();

    @Mock
    private UserFrontRepository repository;

    @Mock
    private UserFront user;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    /*
    @Test
    public void testFindByUser() {
        Mockito.when(repository.findByUsername(Mockito.anyString())).thenReturn(user);
        UserProfileDTO profileDTO = service.findByUser(new UserFront());
        Assert.assertNotNull(profileDTO);
    }
    */

    @Test
    public void testUpdate() {

    }
}
