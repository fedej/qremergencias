package ar.com.utn.proyecto.qremergencias.ws.service;

import ar.com.utn.proyecto.qremergencias.core.domain.MedicalRecord;
import ar.com.utn.proyecto.qremergencias.core.domain.MedicalRecord.MedicalRecordChange;
import ar.com.utn.proyecto.qremergencias.core.domain.User;
import ar.com.utn.proyecto.qremergencias.core.domain.UserFront;
import ar.com.utn.proyecto.qremergencias.core.dto.MedicalRecordDTO;
import ar.com.utn.proyecto.qremergencias.core.mapper.Mapper;
import ar.com.utn.proyecto.qremergencias.core.repository.MedicalRecordRepository;
import ar.com.utn.proyecto.qremergencias.core.repository.UserFrontRepository;
import com.mongodb.gridfs.GridFSFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static ar.com.utn.proyecto.qremergencias.core.domain.MedicalRecord.MedicalRecordChange.Action.CREATE;
import static ar.com.utn.proyecto.qremergencias.core.domain.MedicalRecord.MedicalRecordChange.Action.DELETE;
import static ar.com.utn.proyecto.qremergencias.core.domain.MedicalRecord.MedicalRecordChange.Action.UPDATE;

@Service
public class MedicalRecordService {

    private static final Mapper<MedicalRecordDTO, MedicalRecord> MEDICAL_RECORD_DTO_MAPPER =
            Mapper.mapping(MedicalRecordDTO.class, MedicalRecord.class)
                .constructor((MedicalRecordDTO source) ->
                        new MedicalRecord(source.getName(), source.getText(), source.getPerformed())
                );

    private final MedicalRecordRepository medicalRecordRepository;
    private final UserFrontRepository userFrontRepository;
    private final GridFsTemplate gridFsTemplate;

    @Autowired
    public MedicalRecordService(final MedicalRecordRepository medicalRecordRepository,
                                final UserFrontRepository userFrontRepository,
                                final GridFsTemplate gridFsTemplate) {
        this.medicalRecordRepository = medicalRecordRepository;
        this.userFrontRepository = userFrontRepository;
        this.gridFsTemplate = gridFsTemplate;
    }

    public Page<MedicalRecord> findByUser(final User user, final Pageable page) {
        return medicalRecordRepository.findByUserAndDeletedIsFalse(user, page);
    }

    public MedicalRecord save(final User user, final MedicalRecordDTO medicalRecordDTO,
                              final List<MultipartFile> files) {
        final MedicalRecord medicalRecord = MEDICAL_RECORD_DTO_MAPPER.apply(medicalRecordDTO);
        final UserFront patient = userFrontRepository.findByUsername(medicalRecordDTO.getUser());
        medicalRecord.setUser(patient);
        medicalRecord.getChanges().add(new MedicalRecordChange(CREATE, user));

        if (files != null) {
            files.stream().map(f -> {
                try {
                    return gridFsTemplate.store(f.getInputStream(),
                            f.getOriginalFilename().replace(' ', '_'),
                            f.getContentType());
                } catch (final IOException e) {
                    throw new RuntimeException(e);
                }
            }).map(GridFSFile::getId).forEach(medicalRecord.getFiles()::add);
        }


        return medicalRecordRepository.save(medicalRecord);
    }

    public void update(final String id, final User user, final MedicalRecordDTO medicalRecordDTO) {
        final MedicalRecord medicalRecord = medicalRecordRepository.findOne(id);
        final MedicalRecordChange update = new MedicalRecordChange(UPDATE, user);
        medicalRecord.getChanges().add(update);
        MEDICAL_RECORD_DTO_MAPPER.apply(medicalRecordDTO, medicalRecord);
        medicalRecordRepository.save(medicalRecord);
    }

    public void delete(final String id, final User user) {
        final MedicalRecord medicalRecord = medicalRecordRepository.findOne(id);
        final MedicalRecordChange delete = new MedicalRecordChange(DELETE, user);
        medicalRecord.getChanges().add(delete);
        medicalRecord.setDeleted(true);
        medicalRecordRepository.save(medicalRecord);
    }

    public MedicalRecord findById(final String id) {
        return medicalRecordRepository.findOne(id);
    }

    public Page<MedicalRecord> findByUsername(final String username, final Pageable page) {
        final UserFront user = userFrontRepository.findByUsername(username);
        return medicalRecordRepository.findByUserAndDeletedIsFalse(user, page);
    }
}
