package ar.com.utn.proyecto.qremergencias.ws.service;

import ar.com.utn.proyecto.qremergencias.core.domain.MedicalRecord;
import ar.com.utn.proyecto.qremergencias.core.domain.User;
import ar.com.utn.proyecto.qremergencias.core.repository.MedicalRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class MedicalRecordService {

    @Autowired
    private MedicalRecordRepository medicalRecordRepository;

    public Page<MedicalRecord> findByUser(final User user, final Pageable page) {
        return medicalRecordRepository.findByUser(user, page);
    }
}
