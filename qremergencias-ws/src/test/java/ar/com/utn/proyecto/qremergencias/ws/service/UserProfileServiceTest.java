package ar.com.utn.proyecto.qremergencias.ws.service;

import ar.com.utn.proyecto.qremergencias.core.domain.UserFront;
import ar.com.utn.proyecto.qremergencias.core.dto.UserProfileDTO;
import ar.com.utn.proyecto.qremergencias.core.repository.UserFrontRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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

    @Test
    public void testUpdate() {
        service.update(user, new UserProfileDTO());
        verify(repository, times(2)).save(eq(user));
    }
}
