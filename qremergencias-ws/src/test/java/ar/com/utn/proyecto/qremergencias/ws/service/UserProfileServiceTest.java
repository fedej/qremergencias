package ar.com.utn.proyecto.qremergencias.ws.service;

import ar.com.utn.proyecto.qremergencias.core.domain.UserFront;
import ar.com.utn.proyecto.qremergencias.core.dto.UserProfileDTO;
import ar.com.utn.proyecto.qremergencias.core.repository.UserFrontRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        when(repository.findByUsername(anyString())).thenReturn(new UserFront());
        final UserProfileDTO userProfileDTO = new UserProfileDTO();
        userProfileDTO.setBirthDate(LocalDate.of(1990, 9, 18));
        service.update(user, userProfileDTO, false);
        verify(repository).save(any(UserFront.class));
    }
}
