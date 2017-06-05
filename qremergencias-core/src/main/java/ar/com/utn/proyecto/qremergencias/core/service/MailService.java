package ar.com.utn.proyecto.qremergencias.core.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.List;
import java.util.concurrent.Future;

@Service
@Slf4j
public class MailService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private TemplateEngine templateEngine;


    @Async
    public Future<Boolean> sendMail(final String to, final String subject, final String template,
                         final Context ctx, final List<Resource> images) {

        try {
            final MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            mimeMessage.setFrom();
            final MimeMessageHelper message =
                    new MimeMessageHelper(mimeMessage, true, "UTF-8");
            message.setSubject(subject);
            message.setTo(to);

            final String htmlContent = templateEngine.process(template, ctx);
            message.setText(htmlContent, true);

            for (final Resource image : images) {
                try {
                    message.addInline(image.getFilename(), image, "image/png");
                } catch (MessagingException e) {
                    log.error("ERROR", e);
                }
            }

            javaMailSender.send(mimeMessage);
            return new AsyncResult<>(Boolean.TRUE);
        } catch (MessagingException | MailException e) {
            log.error("ERROR", e);
            return new AsyncResult<>(Boolean.FALSE);
        }

    }

}
