package ar.com.utn.proyecto.qremergencias;

import ar.com.utn.proyecto.qremergencias.core.domain.UserFront;
import ar.com.utn.proyecto.qremergencias.core.domain.emergency.EmergencyData;
import ar.com.utn.proyecto.qremergencias.core.repository.EmergencyDataRepository;
import ar.com.utn.proyecto.qremergencias.core.repository.UserFrontRepository;
import ar.com.utn.proyecto.qremergencias.ws.controller.AuditController;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.javers.core.Javers;
import org.javers.core.commit.CommitMetadata;
import org.javers.core.diff.Change;
import org.javers.core.diff.changetype.ValueChange;
import org.javers.repository.jql.JqlQuery;
import org.javers.repository.jql.QueryBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.*;

@SpringBootApplication(scanBasePackages = "ar.com.utn.proyecto.qremergencias")
@EnableCaching
@EnableAsync
@EntityScan(basePackages = "ar.com.utn.proyecto.qremergencias")
@SuppressWarnings("PMD.UseUtilityClass")
public class QREmergenciasWsApplication {

    public static void main(final String... args) throws JsonProcessingException {
        ConfigurableApplicationContext run = SpringApplication.run(QREmergenciasWsApplication.class, args);

        UserFront rrrramundo = run.getBean(UserFrontRepository.class).findByUsername("paciente@rrramundo.com.ar");
        EmergencyData ed = run.getBean(EmergencyDataRepository.class).findByUser(rrrramundo).get();
        String id1 = ed.getId();
        List<ChangeInner> changesInner = new ArrayList<>();
        changesInner.addAll(getFragmentChanges(run.getBean(Javers.class), "surgeries", id1, ed.getSurgeries()));
        changesInner.addAll(getFragmentChanges(run.getBean(Javers.class), "hospitalizations", id1, ed.getHospitalizations()));
        changesInner.addAll(getFragmentChanges(run.getBean(Javers.class), "medications", id1, ed.getMedications()));
        changesInner.addAll(getFragmentChanges(run.getBean(Javers.class), "pathologies", id1, ed.getPathologies()));

        final List<ChangesDTO> changes = new ArrayList<>();
        changesInner
                .stream()
                .collect(groupingBy(ChangeInner::getId, groupingBy(ChangeInner::getGroup)))
                .forEach((k, v) -> {
                    Map<String, List<ChangeDTO>> copy = v.entrySet()
                            .stream()
                            .collect(toMap(Map.Entry::getKey,
                                    e -> e.getValue()
                                            .stream()
                                            .map(ci -> new ChangeDTO(ci.property, ci.oldValue, ci.newValue))
                                            .collect(toList())
                            ));
                    changes.add(new ChangesDTO(k.id, k.date, k.author, copy));
                });
        System.out.println(run.getBean(ObjectMapper.class).writeValueAsString(changes));

    }

    @Data
    @RequiredArgsConstructor
    @EqualsAndHashCode(of = {"id", "date", "author"})
    @ToString(of = {"id", "date", "author"})
    public static class ChangeDTOId {
        private final String id;
        private final LocalDateTime date;
        private final String author;
    }

    @Data
    @ToString(of = {"property", "oldValue","newValue"})
    @RequiredArgsConstructor
    public static class ChangeDTO {
        private final String property;
        private final Object oldValue;
        private final Object newValue;
    }

    @Data
    private static class ChangeInner {
        private final ChangeDTOId id;
        private final String group;
        private final String property;
        private final Object oldValue;
        private final Object newValue;

        public ChangeInner(Change cambio, String group) {
            CommitMetadata commitMetadata = cambio.getCommitMetadata().get();
            this.id = new ChangeDTOId(commitMetadata.getId().value(),
                    commitMetadata.getCommitDate(), commitMetadata.getAuthor());
            ValueChange vc = (ValueChange) cambio;
            this.group = group;
            this.oldValue = vc.getLeft();
            this.newValue = vc.getRight();
            this.property = vc.getPropertyName();
        }
    }

    @Data
    private static class ChangesDTO {
        private final String id;
        private final LocalDateTime date;
        private final String author;
        private final Map<String, List<ChangeDTO>> changes;
    }

    private static List<ChangeInner> getFragmentChanges(Javers javers, final String fragment, String id1, List elements) {
        List<ChangeInner> changeDTOS = new ArrayList<>();
        for (int i = 0; i < elements.size(); i++) {
            String fragmentId = fragment + "/" + i;
            QueryBuilder qbPathologies = QueryBuilder.byValueObjectId(id1, EmergencyData.class, fragmentId);
            JqlQuery jqlPathologies = qbPathologies.build();
            List<Change> changes = javers.findChanges(jqlPathologies);
            changeDTOS.addAll(changes.stream().map(c -> new ChangeInner(c, fragment)).collect(toList()));
        }
        return changeDTOS;
    }

}
