package ar.com.utn.proyecto.qremergencias.core.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.DBRef;

import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode
@javax.persistence.Entity
@SuppressWarnings("PMD.ImmutableField")
public class MedicalRecord {

    @Id
    @javax.persistence.Id
    private String id;

    @Version
    private Long version;

    @DBRef
    @ManyToOne
    private UserFront user;

    @NotNull
    private final String name;

    @NotNull
    private final String text;

    @NotNull
    private final LocalDate performed;

    private boolean deleted;

    @javax.persistence.ElementCollection
    private final Set<MedicalRecordChange> changes = new HashSet<>();

    @javax.persistence.Transient
    private final Set<Object> files = new HashSet<>();

    public void addAllChanges(final Set<MedicalRecordChange> changes) {
        this.changes.addAll(changes);
    }

    @Data
    @javax.persistence.Entity
    public static class MedicalRecordChange {

        @javax.persistence.Id
        @Setter
        private Long id;

        public enum Action {
            CREATE, DELETE, UPDATE
        }

        private final Action action;

        @NotNull
        private final LocalDateTime timestamp = LocalDateTime.now();

        @DBRef
        @ManyToOne
        private User modifiedBy;

        public MedicalRecordChange(final Action action, final User user) {
            this.action = action;
            this.modifiedBy = user;
        }
    }
}
