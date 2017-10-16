package ar.com.utn.proyecto.qremergencias.ws.service;

import ar.com.utn.proyecto.qremergencias.core.domain.DoctorFront;
import ar.com.utn.proyecto.qremergencias.core.domain.UserFront;
import ar.com.utn.proyecto.qremergencias.core.domain.emergency.EmergencyData;
import ar.com.utn.proyecto.qremergencias.core.dto.emergency.EmergencyDataDTO;
import ar.com.utn.proyecto.qremergencias.core.repository.EmergencyDataRepository;
import ar.com.utn.proyecto.qremergencias.core.repository.UserFrontRepository;
import ar.com.utn.proyecto.qremergencias.core.service.MailService;
import ar.com.utn.proyecto.qremergencias.util.CryptoUtils;
import ar.com.utn.proyecto.qremergencias.util.QRUtils;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.thymeleaf.context.Context;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static ar.com.utn.proyecto.qremergencias.ws.service.DomainMappers.EMERGENCY_DATA_MAPPER;

@Service
public class EmergencyDataService {

    private static final int WIDTH = 340;
    private static final int HEIGHT = 340;
    private static final int WHITE = 255 << 16 | 255 << 8 | 255;
    private static final String CHARSET_NAME = "ISO-8859-1";
    private static final String DATA_CHANGE_SUBJECT = "default.datachange.email.subject";

    private final EmergencyDataRepository repository;
    private final UserFrontRepository userFrontRepository;
    private final GridFsService gridFsService;
    private final MailService mailService;
    private final MessageSource messageSource;
    private final ResourceLoader resourceLoader;

    @Autowired
    public EmergencyDataService(final EmergencyDataRepository repository,
                                final UserFrontRepository userFrontRepository,
                                final GridFsService gridFsService,
                                final MailService mailService,
                                final MessageSource messageSource,
                                final ResourceLoader resourceLoader) {
        this.repository = repository;
        this.userFrontRepository = userFrontRepository;
        this.gridFsService = gridFsService;
        this.mailService = mailService;
        this.messageSource = messageSource;
        this.resourceLoader = resourceLoader;
    }

    public Optional<EmergencyData> findByUser(final String username) {
        final UserFront user = userFrontRepository.findByUsername(username);
        return repository.findByUser(user);
    }

    public Optional<EmergencyData> findByUuid(final String uuid) {
        return repository.findByUuid(uuid);
    }

    public void createOrUpdate(final String username, final EmergencyDataDTO emergencyDataDTO) {
        final UserFront user = userFrontRepository.findByUsername(username);
        final Optional<EmergencyData> oldData = repository.findByUser(user);
        if (oldData.isPresent()) {
            final EmergencyData emergencyData = EMERGENCY_DATA_MAPPER.apply(emergencyDataDTO, oldData.get());
            repository.save(emergencyData);
        } else {
            emergencyDataDTO.setUuid(UUID.randomUUID().toString());
            final EmergencyData emergencyData = EMERGENCY_DATA_MAPPER.apply(emergencyDataDTO);
            emergencyData.setUser(user);
            repository.save(emergencyData);
        }
        sendDataChangeMail(user);
    }

    public Resource getUserQR(final String user) {
        final UserFront userFront = userFrontRepository.findByUsername(user);
        return userFront.getQr() == null ? null : gridFsService.findFileById(userFront.getQr());
    }

    @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
    public void createQR(final String user) {
        final Optional<EmergencyData> emergencyDataOptional = findByUser(user);

        if (emergencyDataOptional.isPresent()) {
            try {
                final EmergencyData emergencyData = emergencyDataOptional.get();
                emergencyData.setUuid(UUID.randomUUID().toString());
                repository.save(emergencyData);
                final byte[] message = QRUtils.encode(emergencyData);
                final String encrypted = CryptoUtils.encryptText(message);
                final Map<EncodeHintType, Object> hints = new ConcurrentHashMap<>(2);
                hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
                hints.put(EncodeHintType.CHARACTER_SET, CHARSET_NAME);
                hints.put(EncodeHintType.MARGIN, 0);
                final BitMatrix bitMatrix = new QRCodeWriter()
                        .encode(encrypted, BarcodeFormat.QR_CODE, WIDTH, HEIGHT, hints);
                final BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
                for (int i = 0; i < WIDTH; i++) {
                    for (int j = 0; j < HEIGHT; j++) {
                        image.setRGB(i, j, bitMatrix.get(i, j) ? 0 : WHITE); // set pixel one by one
                    }
                }

                final UserFront userFront = userFrontRepository.findByUsername(user);
                final Object id = gridFsService.saveQRImage(userFront, image);
                userFront.setQr(id.toString());
                userFrontRepository.save(userFront);
            } catch (IOException | NoSuchAlgorithmException | WriterException | InvalidKeyException
                    | InvalidAlgorithmParameterException | BadPaddingException
                    | NoSuchPaddingException | IllegalBlockSizeException e) {
                throw new RuntimeException(e);
            }
        }

    }

    public void deleteQR(final String username) {
        final UserFront userFront = userFrontRepository.findByUsername(username);
        gridFsService.deleteQR(userFront);
        userFront.setQr(null);
        userFrontRepository.save(userFront);
    }

    private void sendDataChangeMail(final UserFront user) {
        if (!StringUtils.isEmpty(user.getEmail())) {
            final Locale locale = LocaleContextHolder.getLocale();
            final Context ctx = new Context(locale);
            ctx.setVariable("username", user.getUsername());
            final Resource header = resourceLoader
                    .getResource("classpath:static/images/mail/header-mail.jpg");
            final Resource footer = resourceLoader
                    .getResource("classpath:static/images/mail/logo-footer.png");

            mailService.sendMail(user.getEmail(),
                    messageSource.getMessage(DATA_CHANGE_SUBJECT, null, locale), "mail/datachange", ctx,
                    Arrays.asList(header, footer));

        }
    }

}
