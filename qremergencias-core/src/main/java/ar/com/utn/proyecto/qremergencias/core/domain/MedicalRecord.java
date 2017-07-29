package ar.com.utn.proyecto.qremergencias.core.domain;

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

    private boolean deleted;

    private final Set<MedicalRecordChange> changes = new HashSet<>();

    private final Set<Object> files = new HashSet<>();

    public void addAllChanges(final Set<MedicalRecordChange> changes) {
        this.changes.addAll(changes);
    }

    @Value
    @RequiredArgsConstructor
    public static class MedicalRecordChange {

        public enum Action {
            CREATE, DELETE, UPDATE
        }

        private final Action action;

        @NotNull
        private final LocalDateTime timestamp = LocalDateTime.now();

        @DBRef
        private User modifiedBy;
    }
}
