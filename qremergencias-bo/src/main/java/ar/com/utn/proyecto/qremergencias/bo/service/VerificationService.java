package ar.com.utn.proyecto.qremergencias.bo.service;

import ar.com.utn.proyecto.qremergencias.core.domain.DoctorFront;
import ar.com.utn.proyecto.qremergencias.core.domain.User;
import ar.com.utn.proyecto.qremergencias.core.domain.UserFront;
import ar.com.utn.proyecto.qremergencias.core.domain.UserVerificationToken;
import ar.com.utn.proyecto.qremergencias.core.repository.UserFrontRepository;
import ar.com.utn.proyecto.qremergencias.core.repository.UserVerificationTokenRepository;
import ar.com.utn.proyecto.qremergencias.core.service.MailService;
import com.mongodb.gridfs.GridFSDBFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.thymeleaf.context.Context;

import java.util.Arrays;
import java.util.Locale;
import java.util.UUID;

@Service
public class VerificationService {

    private static final String MEDICO = "ROLE_MEDICO";
    private static final String GREETING_SUBJECT = "default.greeting.email.subject";

    @Value("${qremergencias.front.completeRegistrationUrl}")
    private String completeRegistrationUrl;

    private final UserFrontRepository userFrontRepository;

    private final UserVerificationTokenRepository userTokenRepository;


    @Autowired
    private MailService mailService;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private final GridFsTemplate gridFsTemplate;

    @Autowired
    public VerificationService(final UserFrontRepository userFrontRepository,
                               final UserVerificationTokenRepository userTokenRepository,
                               final GridFsTemplate gridFsTemplate) {
        this.userFrontRepository = userFrontRepository;
        this.userTokenRepository = userTokenRepository;
        this.gridFsTemplate = gridFsTemplate;
    }

    public Page<DoctorFront> findMedicos(final Pageable page) {
        return userFrontRepository.findByRolesContaining(MEDICO, page);
    }

    public void verify(final String id) {
        modifyMedico(id, true);
    }

    public void unverify(final String id) {
        modifyMedico(id, false);
    }

    private void modifyMedico(final String id, final boolean verified) {
        final UserFront medico = userFrontRepository.findOne(id);
        medico.setEnabled(verified);
        final UserFront savedUser = userFrontRepository.save(medico);
        final String token = UUID.randomUUID().toString();
        createVerificationToken(savedUser, token);
        sendMailConfirmation(savedUser);
    }

    public void createVerificationToken(final User user, final String token) {
        final UserVerificationToken uvt = new UserVerificationToken(user, token);
        userTokenRepository.save(uvt);
    }

    private void sendMailConfirmation(final UserFront user) {

        if (!StringUtils.isEmpty(user.getEmail())) {
            final Locale locale = LocaleContextHolder.getLocale();
            final Context ctx = new Context(locale);

            ctx.setVariable("username", user.getUsername());
            ctx.setVariable("url",
                    completeRegistrationUrl
                            + userTokenRepository.findByUser(user).getToken());

            final Resource header = resourceLoader
                    .getResource("classpath:static/images/mail/header-mail.jpg");

            final Resource button = resourceLoader
                    .getResource("classpath:static/images/mail/btn-codigo.png");

            final Resource footer = resourceLoader
                    .getResource("classpath:static/images/mail/logo-footer.png");

            mailService.sendMail(user.getEmail(),
                    messageSource.getMessage(GREETING_SUBJECT, null, locale), "mail/greeting", ctx,
                    Arrays.asList(header, button, footer));
        }
    }

    public GridFSDBFile downloadEvidenceFile(final String id) {
        final DoctorFront doctor = (DoctorFront) userFrontRepository.findOne(id);
        final Object evidence = doctor.getEvidenceFile();
        if (evidence != null) {
            return gridFsTemplate.findOne(new Query(Criteria.where("_id").is(evidence.toString())));
        }
        return null;
    }
}
