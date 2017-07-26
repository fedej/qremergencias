package ar.com.utn.proyecto.qremergencias.ws.service;

import ar.com.utn.proyecto.qremergencias.core.domain.MedicalRecord;
import ar.com.utn.proyecto.qremergencias.core.domain.MedicalRecord.MedicalRecordChange;
import ar.com.utn.proyecto.qremergencias.core.domain.User;
import ar.com.utn.proyecto.qremergencias.core.domain.UserFront;
import ar.com.utn.proyecto.qremergencias.core.dto.MedicalRecordDTO;
import ar.com.utn.proyecto.qremergencias.core.mapper.Mapper;
import ar.com.utn.proyecto.qremergencias.core.repository.MedicalRecordRepository;
import ar.com.utn.proyecto.qremergencias.core.repository.UserFrontRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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

    @Autowired
    private MedicalRecordRepository medicalRecordRepository;

    @Autowired
    private UserFrontRepository userFrontRepository;

    public Page<MedicalRecord> findByUser(final User user, final Pageable page) {
        return medicalRecordRepository.findByUserAndDeletedIsFalse(user, page);
    }

    public MedicalRecord save(final User user, final MedicalRecordDTO medicalRecordDTO) {
        final MedicalRecord medicalRecord = MEDICAL_RECORD_DTO_MAPPER.apply(medicalRecordDTO);
        final UserFront patient = userFrontRepository.findOne(medicalRecordDTO.getUser());
        medicalRecord.setUser(patient);
        medicalRecord.getChanges().add(new MedicalRecordChange(CREATE, user));
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
}
