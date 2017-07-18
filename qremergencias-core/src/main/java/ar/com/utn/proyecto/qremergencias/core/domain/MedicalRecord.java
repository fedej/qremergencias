package ar.com.utn.proyecto.qremergencias.core.domain;

import com.mongodb.gridfs.GridFSFile;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.DBRef;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode
@SuppressWarnings("PMD.ImmutableField")
public class MedicalRecord {

    @Id
    private String id;

    @Version
    private Long version;

    @DBRef
    private UserFront user;

    @NotNull
    private final String name;

    @NotNull
    private final String text;

    @NotNull
    private final LocalDate performed;

    private final Set<MedicalRecordChange> changes = new HashSet<>();

    @DBRef
    private final Set<GridFSFile> files = new HashSet<>();

    @Value
    @RequiredArgsConstructor
    public static class MedicalRecordChange {

        public enum Action {
            CREATE, DELETE, UPDATE
        }

        private Action action = Action.CREATE;

        @NotNull
        private final LocalDateTime timestamp;

        @DBRef
        private final User modifiedBy;
    }
}
