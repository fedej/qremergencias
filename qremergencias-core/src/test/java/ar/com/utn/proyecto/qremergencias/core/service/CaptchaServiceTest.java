package ar.com.utn.proyecto.qremergencias.core.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

public class CaptchaServiceTest {

    private static final String URL = "http://localhost";

    @InjectMocks
    private CaptchaService service;

    @Mock
    private RestTemplate restTemplate;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(service, "recaptchaUrl", URL);

    }

    @Test
    public void testValidateNull() {
        assertFalse(service.validate(null, null));
        assertFalse(service.validate(null, "notNull"));
        assertFalse(service.validate("notNull", null));
    }

    @Test
    public void testValidate() throws UnknownHostException {

        final CaptchaService.RecaptchaResponse response = new CaptchaService.RecaptchaResponse();
        response.setErrorCodes(Collections.EMPTY_LIST);
        response.setSuccess(true);

        when(restTemplate.postForEntity(eq(URL), any(MultiValueMap.class), any(Class.class)))
                .thenReturn(new ResponseEntity<>(response, HttpStatus.OK));

        assertTrue(service.validate(InetAddress.getLocalHost().getHostAddress(), "asdasd"));

        response.setSuccess(false);
        response.setErrorCodes(Arrays.asList("invalid captcha"));
        assertFalse(service.validate(InetAddress.getLocalHost().getHostAddress(), "asdasd"));
    }

}
