package ar.com.utn.proyecto.qremergencias.ws.service;

import ar.com.utn.proyecto.qremergencias.core.domain.UserFront;
import ar.com.utn.proyecto.qremergencias.core.domain.emergency.EmergencyData;
import ar.com.utn.proyecto.qremergencias.core.dto.emergency.EmergencyDataDTO;
import ar.com.utn.proyecto.qremergencias.core.dto.emergency.changelog.ChangeDTO;
import ar.com.utn.proyecto.qremergencias.core.dto.emergency.changelog.ChangeDTOId;
import ar.com.utn.proyecto.qremergencias.core.dto.emergency.changelog.ChangesDTO;
import ar.com.utn.proyecto.qremergencias.core.repository.EmergencyDataRepository;
import ar.com.utn.proyecto.qremergencias.core.repository.UserFrontRepository;
import lombok.Data;
import org.javers.core.Javers;
import org.javers.core.commit.CommitMetadata;
import org.javers.core.diff.Change;
import org.javers.core.diff.changetype.ValueChange;
import org.javers.core.diff.changetype.container.ListChange;
import org.javers.core.metamodel.object.ValueObjectId;
import org.javers.repository.jql.JqlQuery;
import org.javers.repository.jql.QueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static ar.com.utn.proyecto.qremergencias.ws.service.DomainMappers.EMERGENCY_DATA_MAPPER;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

@Service
public class EmergencyDataService {

    private final EmergencyDataRepository repository;
    private final UserFrontRepository userFrontRepository;
    private final Javers javers;

    @Autowired
    public EmergencyDataService(final EmergencyDataRepository repository,
                                final UserFrontRepository userFrontRepository,
                                final Javers javers) {
        this.repository = repository;
        this.userFrontRepository = userFrontRepository;
        this.javers = javers;
    }

    public Optional<EmergencyData> findByUser(final String username) {
        final UserFront user = userFrontRepository.findByUsername(username);
        return repository.findByUser(user);
    }

    public void createOrUpdate(final String username, final EmergencyDataDTO emergencyDataDTO) {
        final UserFront user = userFrontRepository.findByUsername(username);
        final Optional<EmergencyData> oldData = repository.findByUser(user);

        if (oldData.isPresent()) {
            final EmergencyData emergencyData = EMERGENCY_DATA_MAPPER.apply(emergencyDataDTO, oldData.get());
            repository.save(emergencyData);
        } else {
            final EmergencyData emergencyData = EMERGENCY_DATA_MAPPER.apply(emergencyDataDTO);
            emergencyData.setUser(user);
            repository.save(emergencyData);
        }

    }

    @SuppressWarnings("PMD.UseConcurrentHashMap")
    public PageImpl<ChangesDTO> getEmergencyDataChanges(final UserFront user) {
        final Optional<EmergencyData> optionalData = repository.findByUser(user);

        if (!optionalData.isPresent()) {
            return new PageImpl<>(Collections.emptyList());
        }

        final EmergencyData ed = optionalData.get();
        final String id1 = ed.getId();
        final QueryBuilder qbPathologies = QueryBuilder.byInstanceId(id1, EmergencyData.class).withChildValueObjects();
        final JqlQuery jqlPathologies = qbPathologies.build();

        return new PageImpl<>(javers.findChanges(jqlPathologies)
                .stream()
                .filter(c -> (c instanceof ValueChange || c instanceof ListChange) && c.getAffectedGlobalId()
                        instanceof ValueObjectId)
                .map(ChangeInner::new)
                .collect(groupingBy(ChangeInner::getId, groupingBy(ChangeInner::getGroup, mapping(
                        this::mapChangeInnerToDTO,
                        toList()
                ))))
                .entrySet()
                .stream()
                .map(entry -> {
                    final ChangeDTOId key = entry.getKey();
                    return new ChangesDTO(key.getId(), key.getDate(), key.getAuthor(), entry.getValue());
                })
                .sorted(comparing(ChangesDTO::getDate).reversed())
                .collect(toList()));
    }

    private ChangeDTO mapChangeInnerToDTO(final ChangeInner changeInner) {
        return new ChangeDTO(changeInner.getProperty(), changeInner.getOldValue(), changeInner.getNewValue(),
                changeInner.getAdded(), changeInner.getRemoved());
    }


    @Data
    private static class ChangeInner {
        private final ChangeDTOId id;
        private final String group;
        private final String property;
        private Object oldValue;
        private Object newValue;
        private List<String> added;
        private List<String> removed;

        private ChangeInner(final Change cambio) {
            final CommitMetadata commitMetadata = cambio.getCommitMetadata().get();
            this.id = new ChangeDTOId(commitMetadata.getId().value(),
                    commitMetadata.getCommitDate(), commitMetadata.getAuthor());

            if (cambio instanceof ValueChange && cambio.getAffectedGlobalId() instanceof ValueObjectId) {
                final ValueObjectId globalId = (ValueObjectId) cambio.getAffectedGlobalId();
                final String fragment = globalId.getFragment();
                final boolean isList = fragment.contains("/");
                final int slash = fragment.lastIndexOf('/');
                this.group = isList ? fragment.substring(0, slash) : fragment;

                final ValueChange vc = (ValueChange) cambio;
                this.oldValue = vc.getLeft();
                this.newValue = vc.getRight();
                this.property = isList ? group
                        + "["
                        + fragment.substring(slash + 1, fragment.length())
                        + "]."
                        + vc.getPropertyName() : vc.getPropertyName();
            } else if (cambio instanceof ListChange && cambio.getAffectedGlobalId() instanceof ValueObjectId) {
                final ValueObjectId globalId = (ValueObjectId) cambio.getAffectedGlobalId();
                final String fragment = globalId.getFragment();
                final boolean isList = fragment.contains("/");
                final int slash = fragment.lastIndexOf('/');
                this.group = isList ? fragment.substring(0, slash) : fragment;

                final ListChange lc = (ListChange) cambio;
                this.added = lc.getAddedValues().stream().map(Object::toString).collect(toList());
                this.removed = lc.getRemovedValues().stream().map(Object::toString).collect(toList());
                this.property = lc.getPropertyName();
            } else {
                this.group = "";
                this.property = "";
            }

        }
    }
}
