package ar.com.utn.proyecto.qremergencias.ws.service;

import ar.com.utn.proyecto.qremergencias.core.domain.UserEmergencyContact;
import ar.com.utn.proyecto.qremergencias.core.domain.UserFront;
import ar.com.utn.proyecto.qremergencias.core.domain.emergency.EmergencyData;
import ar.com.utn.proyecto.qremergencias.core.domain.emergency.GeneralData;
import ar.com.utn.proyecto.qremergencias.core.domain.emergency.Pathology;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

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
    private static final String CHARSET_NAME = "ISO-8859-1";

    private final EmergencyDataRepository repository;
    private final UserFrontRepository userFrontRepository;
    private final Javers javers;
    private final GridFsService gridFsService;

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

    @SuppressWarnings("PMD")
    public void createQR(final UserFront user) {
        final Optional<EmergencyData> emergencyDataOptional = findByUser(user.getUsername());

        if (emergencyDataOptional.isPresent()) {
            try {
                final EmergencyData emergencyData = emergencyDataOptional.get();
                final GeneralData general = emergencyData.getGeneral();
                final List<String> patos = emergencyData.getPathologies()
                        .stream().map(Pathology::getDescription).collect(toList());

                final Integer contactsLength = user.getContacts() != null && !user.getContacts().isEmpty()
                        ? user.getContacts().get(0).getFirstName().length()
                        + user.getContacts().get(0).getPhoneNumber().length() + 2
                        : 0;

                // Byte 0 a 2 Tipo de sangre
                final String bloodType = general.getBloodType();
                final StringBuilder sb = new StringBuilder();
                for (int i = 0; i < 64; i++) {
                    sb.append(i);
                }
                final String url = "https://www.google.com.ar/search?q=" + sb.toString();

                final byte[] message = new byte[7 + url.length() + 1 + contactsLength];
                message[0] = (byte) bloodType.charAt(0);
                message[1] = (byte) bloodType.charAt(1);
                message[2] = bloodType.length() > 2 ? (byte) bloodType.charAt(2) : 0;

                short year = (short) user.getBirthdate().getYear();
                message[3] = (byte)(year & 0xff);
                message[4] = (byte)((year >> 8) & 0xff);
                message[5] = (byte) user.getSex();

                // Byte 6 Alergias y patologias comunes
                BitSet bitSet = BitSet.valueOf(message);
                if (general.getAllergies().contains("Penicilina")) {
                    bitSet.set(48);
                }
                if (general.getAllergies().contains("Insulina")) {
                    bitSet.set(49);
                }
                if (general.getAllergies().contains("Rayos X con yodo")) {
                    bitSet.set(50);
                }
                if (general.getAllergies().contains("Sulfamidas")) {
                    bitSet.set(51);
                }
                if (patos.contains("Hipertension")) {
                    bitSet.set(52);
                }
                if (patos.contains("Asma")) {
                    bitSet.set(53);
                }
                if (patos.contains("Antecedentes Oncologicos")) {
                    bitSet.set(54);
                }
                if (patos.contains("Insuficiencia Suprarrenal")) {
                    bitSet.set(55);
                }

                final byte[] urlBytes = url.getBytes(CHARSET_NAME);
                int bufferPosition = 7;
                System.arraycopy(urlBytes, 0, message, bufferPosition, urlBytes.length);
                bufferPosition += urlBytes.length;
                message[bufferPosition] = '\0';
                bufferPosition++;

                if (user.getContacts() != null && !user.getContacts().isEmpty()) {
                    UserEmergencyContact contact = user.getContacts().get(0);
                    final byte[] nameBytes = contact.getFirstName().getBytes(CHARSET_NAME);
                    System.arraycopy(nameBytes, 0, message, bufferPosition, nameBytes.length);
                    bufferPosition += nameBytes.length;
                    message[bufferPosition] = '\0';
                    bufferPosition++;

                    final byte[] phoneBytes = contact.getPhoneNumber().getBytes(CHARSET_NAME);
                    System.arraycopy(phoneBytes, 0, message, bufferPosition, phoneBytes.length);
                    bufferPosition += phoneBytes.length;
                    message[bufferPosition] = '\0';
                }

                final String encrypted = CryptoUtils.encryptText(message);
                final Map<EncodeHintType, Object> hints = new ConcurrentHashMap<>(2);
                hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
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
                final Object id = gridFsService.saveQRImage(user, image);
                user.setQr(id.toString());
                userFrontRepository.save(user);
            } catch (IOException | NoSuchAlgorithmException | WriterException | InvalidKeyException
                    | InvalidAlgorithmParameterException | BadPaddingException
                    | NoSuchPaddingException | IllegalBlockSizeException e) {
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
