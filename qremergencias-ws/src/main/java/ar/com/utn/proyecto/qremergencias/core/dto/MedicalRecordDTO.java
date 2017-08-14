package ar.com.utn.proyecto.qremergencias.core.dto;

import ar.com.utn.proyecto.qremergencias.core.domain.MedicalRecord;
import ar.com.utn.proyecto.qremergencias.core.domain.User;
import com.google.common.io.Files;
import com.mongodb.gridfs.GridFSDBFile;
import io.swagger.annotations.ApiParam;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.util.UriTemplate;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class MedicalRecordDTO {

    @ApiParam(readOnly = true, hidden = true)
    private String id;

    @NotEmpty
    private String name;

    @NotEmpty
    private String text;

    @NotNull
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate performed;

    @NotEmpty
    private String user;

    @ApiParam(readOnly = true, hidden = true)
    private final SortedSet<MedicalRecordChangeDTO> changes = new TreeSet<>();

    @ApiParam(readOnly = true, hidden = true)
    private final Set<FileDTO> files = new HashSet<>();

    public MedicalRecordDTO(final MedicalRecord medicalRecord, final UriTemplate uriTemplate,
                            final Function<String, GridFSDBFile> finder) {
        this.id = medicalRecord.getId();
        this.name = medicalRecord.getName();
        this.text = medicalRecord.getText();
        this.performed = medicalRecord.getPerformed();
        this.user = medicalRecord.getUser().getId();
        this.changes.addAll(medicalRecord.getChanges()
                .stream()
                .map(MedicalRecordChangeDTO::new)
                .collect(Collectors.toSet()));

        this.files.addAll(medicalRecord
                .getFiles()
                .stream()
                .map(id -> {
                    final GridFSDBFile file = finder.apply(id.toString());
                    return new FileDTO(uriTemplate.expand(id.toString()).toString(),
                            file.getContentType(), Files.getFileExtension(file.getFilename()));
                })
                .collect(Collectors.toList()));
    }

    public MedicalRecordDTO(final String name, final String text, final LocalDate performed, final String user) {
        this.name = name;
        this.text = text;
        this.performed = performed;
        this.user = user;
    }

    @Value
    private static class MedicalRecordChangeDTO implements Comparable<MedicalRecordChangeDTO> {

        @ApiParam(readOnly = true, hidden = true)
        private final String action;

        @ApiParam(readOnly = true, hidden = true)
        private final LocalDateTime timestamp;

        @ApiParam(readOnly = true, hidden = true)
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

    @Data
    @RequiredArgsConstructor
    private static class FileDTO {

        @ApiParam(readOnly = true, hidden = true)
        private final String url;

        @ApiParam(readOnly = true, hidden = true)
        private final String mimeType;

        @ApiParam(readOnly = true, hidden = true)
        private final String extension;
    }
}
