package ar.com.utn.proyecto.qremergencias.core.dto;

import ar.com.utn.proyecto.qremergencias.core.domain.MedicalRecord;
import ar.com.utn.proyecto.qremergencias.core.domain.User;
import com.mongodb.gridfs.GridFSFile;
import lombok.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Value
public class MedicalRecordDTO {

    private final String id;
    private final String name;
    private final String text;
    private final LocalDate performed;
    private final SortedSet<MedicalRecordChangeDTO> changes = new TreeSet<>();
    private final Set<Object> files = new HashSet<>();

    public MedicalRecordDTO(final MedicalRecord medicalRecord) {
        this.id = medicalRecord.getId();
        this.name = medicalRecord.getName();
        this.text = medicalRecord.getText();
        this.performed = medicalRecord.getPerformed();
        this.changes.addAll(medicalRecord.getChanges()
                .stream()
                .map(MedicalRecordChangeDTO::new)
                .collect(Collectors.toSet()));

        this.files.addAll(medicalRecord.getFiles()
                .stream()
                .map(GridFSFile::getId)
                .collect(Collectors.toSet()));
    }

    @Value
    private static class MedicalRecordChangeDTO implements Comparable<MedicalRecordChangeDTO> {

        private final String action;
        private final LocalDateTime timestamp;
        private final String modifiedBy;

        MedicalRecordChangeDTO(final MedicalRecord.MedicalRecordChange medicalRecordChange) {
            this.action = medicalRecordChange.getAction().toString();
            this.timestamp = medicalRecordChange.getTimestamp();
            final User modifiedBy = medicalRecordChange.getModifiedBy();
            this.modifiedBy = modifiedBy != null ? modifiedBy.getUsername() : null;
        }

        @Override
        public int compareTo(final MedicalRecordChangeDTO other) {
            return timestamp.compareTo(other.timestamp);
        }

    }
}
