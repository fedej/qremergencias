package ar.com.utn.proyecto.qremergencias.ws.service;

import ar.com.utn.proyecto.qremergencias.core.domain.UserFront;
import ar.com.utn.proyecto.qremergencias.core.domain.emergency.EmergencyData;
import ar.com.utn.proyecto.qremergencias.core.domain.emergency.GeneralData;
import ar.com.utn.proyecto.qremergencias.core.dto.emergency.EmergencyDataDTO;
import ar.com.utn.proyecto.qremergencias.core.dto.emergency.changelog.ChangeDTO;
import ar.com.utn.proyecto.qremergencias.core.dto.emergency.changelog.ChangeDTOId;
import ar.com.utn.proyecto.qremergencias.core.dto.emergency.changelog.ChangesDTO;
import ar.com.utn.proyecto.qremergencias.core.repository.EmergencyDataRepository;
import ar.com.utn.proyecto.qremergencias.core.repository.UserFrontRepository;
import ar.com.utn.proyecto.qremergencias.util.CryptoUtils;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.Data;
import org.javers.core.Javers;
import org.javers.core.commit.CommitMetadata;
import org.javers.core.diff.Change;
import org.javers.core.diff.changetype.ValueChange;
import org.javers.core.diff.changetype.container.ListChange;
import org.javers.core.metamodel.object.ValueObjectId;
import org.javers.repository.jql.JqlQuery;
import org.javers.repository.jql.QueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static ar.com.utn.proyecto.qremergencias.util.CryptoUtils.getPrivate;
import static ar.com.utn.proyecto.qremergencias.ws.service.DomainMappers.EMERGENCY_DATA_MAPPER;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

@Service
public class EmergencyDataService {

    private static final int WIDTH = 340;
    private static final int HEIGHT = 340;
    private static final int WHITE = 255 << 16 | 255 << 8 | 255;

    private final EmergencyDataRepository repository;
    private final UserFrontRepository userFrontRepository;
    private final Javers javers;
    private final GridFsService gridFsService;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    public EmergencyDataService(final EmergencyDataRepository repository,
                                final UserFrontRepository userFrontRepository,
                                final Javers javers, final GridFsService gridFsService) {
        this.repository = repository;
        this.userFrontRepository = userFrontRepository;
        this.javers = javers;
        this.gridFsService = gridFsService;
    }

    public Optional<EmergencyData> findByUser(final String username) {
        final UserFront user = userFrontRepository.findByUsername(username);
        return repository.findByUser(user);
    }

    public void createOrUpdate(final String username, final EmergencyDataDTO emergencyDataDTO) {
        final UserFront user = userFrontRepository.findByUsername(username);
        final Optional<EmergencyData> oldData = repository.findByUser(user);

        if (oldData.isPresent()) {
            final EmergencyData emergencyData = EMERGENCY_DATA_MAPPER.apply(emergencyDataDTO, oldData.get());
            repository.save(emergencyData);
        } else {
            final EmergencyData emergencyData = EMERGENCY_DATA_MAPPER.apply(emergencyDataDTO);
            emergencyData.setUser(user);
            repository.save(emergencyData);
        }

    }

    @SuppressWarnings("PMD.UseConcurrentHashMap")
    public PageImpl<ChangesDTO> getEmergencyDataChanges(final UserFront user) {
        final Optional<EmergencyData> optionalData = repository.findByUser(user);

        if (!optionalData.isPresent()) {
            return new PageImpl<>(Collections.emptyList());
        }

        final EmergencyData ed = optionalData.get();
        final String id1 = ed.getId();
        final QueryBuilder qbPathologies = QueryBuilder.byInstanceId(id1, EmergencyData.class).withChildValueObjects();
        final JqlQuery jqlPathologies = qbPathologies.build();

        return new PageImpl<>(javers.findChanges(jqlPathologies)
                .stream()
                .filter(c -> (c instanceof ValueChange || c instanceof ListChange) && c.getAffectedGlobalId()
                        instanceof ValueObjectId)
                .map(ChangeInner::new)
                .collect(groupingBy(ChangeInner::getId, groupingBy(ChangeInner::getGroup, mapping(
                        this::mapChangeInnerToDTO,
                        toList()
                ))))
                .entrySet()
                .stream()
                .map(entry -> {
                    final ChangeDTOId key = entry.getKey();
                    return new ChangesDTO(key.getId(), key.getDate(), key.getAuthor(), entry.getValue());
                })
                .sorted(comparing(ChangesDTO::getDate).reversed())
                .collect(toList()));
    }

    private ChangeDTO mapChangeInnerToDTO(final ChangeInner changeInner) {
        return new ChangeDTO(changeInner.getProperty(), changeInner.getOldValue(), changeInner.getNewValue(),
                changeInner.getAdded(), changeInner.getRemoved());
    }

    public Resource getUserQR(final UserFront user) {
        return gridFsService.findFileById(user.getQr());
    }

    @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
    public void createQR(final UserFront user) {
        final Optional<EmergencyData> emergencyDataOptional = findByUser(user.getUsername());

        if (emergencyDataOptional.isPresent()) {
            try {
                final EmergencyData emergencyData = emergencyDataOptional.get();
                final GeneralData general = emergencyData.getGeneral();

                final Integer allergiesLength = general.getAllergies()
                        .stream()
                        .mapToInt(s -> s.length() + 1).sum();


                // Byte 0 a 2 Tipo de sangre
                final String bloodType = general.getBloodType();
                final byte[] message = new byte[4 + allergiesLength];
                message[0] = (byte) bloodType.charAt(0);
                message[1] = (byte) bloodType.charAt(1);
                message[2] = bloodType.length() > 2 ? (byte) bloodType.charAt(2) : 0;


                if (general.isOrganDonor()) {
                    // Byte 3 Donante de organos
                    message[3] = 1;
                }

                int bufferPosition = 4;
                for (final String allergy : general.getAllergies()) {
                    final byte[] stringBytes = allergy.getBytes(Charset.forName("UTF-8"));
                    System.arraycopy(stringBytes, 0, message, bufferPosition, stringBytes.length);
                    bufferPosition += stringBytes.length;  // advance index
                    message[bufferPosition] = '\0';
                    bufferPosition++;
                }

                final PrivateKey privateKey = getPrivate(resourceLoader.getResource("classpath://keyPair/privateKey"));
                final String encrypted = CryptoUtils.encryptText(message, privateKey);
                final Map<EncodeHintType, Object> hints = new ConcurrentHashMap<>(2);
                hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
                hints.put(EncodeHintType.MARGIN, 0);
                final BitMatrix bitMatrix = new QRCodeWriter()
                        .encode(encrypted, BarcodeFormat.QR_CODE, WIDTH, HEIGHT, hints);
                final BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
                for (int i = 0; i < WIDTH; i++) {
                    for (int j = 0; j < HEIGHT; j++) {
                        image.setRGB(i, j, bitMatrix.get(i, j) ? 0 : WHITE); // set pixel one by one
                    }
                }
                final Object id = gridFsService.saveQRImage(user, image);
                user.setQr(id.toString());
                userFrontRepository.save(user);
            } catch (IOException | NoSuchAlgorithmException | WriterException | InvalidKeyException
                    | BadPaddingException | NoSuchPaddingException | InvalidKeySpecException
                    | IllegalBlockSizeException e) {
                throw new RuntimeException(e);
            }
        }

    }

    @Data
    private static class ChangeInner {
        private final ChangeDTOId id;
        private final String group;
        private final String property;
        private Object oldValue;
        private Object newValue;
        private List<String> added;
        private List<String> removed;

        private ChangeInner(final Change cambio) {
            final CommitMetadata commitMetadata = cambio.getCommitMetadata().get();
            this.id = new ChangeDTOId(commitMetadata.getId().value(),
                    commitMetadata.getCommitDate(), commitMetadata.getAuthor());

            if (cambio instanceof ValueChange && cambio.getAffectedGlobalId() instanceof ValueObjectId) {
                final ValueObjectId globalId = (ValueObjectId) cambio.getAffectedGlobalId();
                final String fragment = globalId.getFragment();
                final boolean isList = fragment.contains("/");
                final int slash = fragment.lastIndexOf('/');
                this.group = isList ? fragment.substring(0, slash) : fragment;

                final ValueChange vc = (ValueChange) cambio;
                this.oldValue = vc.getLeft();
                this.newValue = vc.getRight();
                this.property = isList ? group
                        + "["
                        + fragment.substring(slash + 1, fragment.length())
                        + "]."
                        + vc.getPropertyName() : vc.getPropertyName();
            } else if (cambio instanceof ListChange && cambio.getAffectedGlobalId() instanceof ValueObjectId) {
                final ValueObjectId globalId = (ValueObjectId) cambio.getAffectedGlobalId();
                final String fragment = globalId.getFragment();
                final boolean isList = fragment.contains("/");
                final int slash = fragment.lastIndexOf('/');
                this.group = isList ? fragment.substring(0, slash) : fragment;

                final ListChange lc = (ListChange) cambio;
                this.added = lc.getAddedValues().stream().map(Object::toString).collect(toList());
                this.removed = lc.getRemovedValues().stream().map(Object::toString).collect(toList());
                this.property = lc.getPropertyName();
            } else {
                this.group = "";
                this.property = "";
            }

        }
    }
}
