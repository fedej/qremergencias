package ar.com.utn.proyecto.qremergencias.core.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.IContext;

import javax.mail.internet.MimeMessage;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(TemplateEngine.class)
public class MailServiceTest {

    @InjectMocks
    private MailService service;

    @Mock
    private JavaMailSender javaMailSender;

    private final TemplateEngine templateEngine = PowerMockito.mock(TemplateEngine.class);

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testSendMail() throws ExecutionException, InterruptedException {
        final MimeMessage message = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(message);
        when(templateEngine.process(anyString(), any(IContext.class))).thenReturn("html");

        final Future<Boolean> result = service.sendMail("to", "subject", "template", new Context(),
                Collections.EMPTY_LIST);

        assertTrue(result.get());
        verify(javaMailSender).send(message);
    }

    @Test
    public void testSendMailFails() throws ExecutionException, InterruptedException {
        final MimeMessage message = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(message);
        when(templateEngine.process(anyString(), any(IContext.class))).thenReturn("html");
        doThrow(new MailSendException("Test error")).when(javaMailSender).send(message);

        final Future<Boolean> result = service.sendMail("to", "subject", "template", new Context(),
                Collections.EMPTY_LIST);
        assertFalse(result.get());

    }

}
