package ar.com.utn.proyecto.qremergencias.core.repository;

import ar.com.utn.proyecto.qremergencias.core.domain.MedicalRecord;
import ar.com.utn.proyecto.qremergencias.core.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;

public interface MedicalRecordRepository extends MongoRepository<MedicalRecord, String> {

    Page<MedicalRecord> findByUserAndDeletedIsFalse(User user, Pageable pageable);

    Page<MedicalRecord> findByUserAndDeletedIsFalseAndPerformedBetweenAndTextLike(User user, LocalDate from,
                                                                          LocalDate to, String text, Pageable pageable);

}
