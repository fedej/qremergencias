package ar.com.utn.proyecto.qremergencias.changes;

import ar.com.utn.proyecto.qremergencias.core.dto.emergency.changelog.ChangeDTO;
import ar.com.utn.proyecto.qremergencias.core.dto.emergency.changelog.ChangesDTO;
import org.javers.core.changelog.ChangeProcessor;
import org.javers.core.commit.CommitMetadata;
import org.javers.core.diff.Change;
import org.javers.core.diff.changetype.NewObject;
import org.javers.core.diff.changetype.ObjectRemoved;
import org.javers.core.diff.changetype.PropertyChange;
import org.javers.core.diff.changetype.ReferenceChange;
import org.javers.core.diff.changetype.ValueChange;
import org.javers.core.diff.changetype.container.ArrayChange;
import org.javers.core.diff.changetype.container.ContainerChange;
import org.javers.core.diff.changetype.container.ListChange;
import org.javers.core.diff.changetype.container.SetChange;
import org.javers.core.diff.changetype.map.MapChange;
import org.javers.core.metamodel.object.GlobalId;
import org.javers.core.metamodel.object.InstanceId;
import org.javers.core.metamodel.object.ValueObjectId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.stream.Collectors.toList;

@SuppressWarnings("PMD.TooManyMethods")
public class TableChangeProcessor implements ChangeProcessor<List<ChangesDTO>> {

    private static final Comparator<ChangesDTO> CHANGES_DTO_COMPARATOR =
            Comparator.comparing(ChangesDTO::getId).thenComparing(ChangesDTO::getDate).reversed();

    private final Map<String, ChangesDTO> changesMap = new ConcurrentHashMap<>();

    @Override
    public void onCommit(final CommitMetadata mdata) {
        final String id = mdata.getId().value();
        changesMap.put(id, new ChangesDTO(id, mdata.getCommitDate(), mdata.getAuthor(), new ConcurrentHashMap<>()));
    }

    @Override
    public void onAffectedObject(final GlobalId globalId) {

    }

    @Override
    public void beforeChangeList() {

    }

    @Override
    public void afterChangeList() {

    }

    @Override
    public void beforeChange(final Change change) {

    }

    @Override
    public void afterChange(final Change change) {

    }

    @Override
    public void onPropertyChange(final PropertyChange propertyChange) {

    }

    @Override
    public void onValueChange(final ValueChange cambio) {

        if (cambio.getPropertyName().equalsIgnoreCase("version")
                || !(cambio.getAffectedGlobalId().toString().contains("#"))) {
            return;
        }

        final String left = cambio.getLeft() == null ? null : cambio.getLeft().toString();
        final String right = cambio.getRight() == null ? null : cambio.getRight().toString();
        final String section = getSection(cambio.getAffectedGlobalId());
        cambio.getCommitMetadata().ifPresent(commitMetadata -> {
            final ChangeDTO changeDTO = new ChangeDTO(cambio.getPropertyName(), left, right, null, null);
            changesMap.get(commitMetadata.getId().value()).addChange(section, changeDTO);
        });

    }

    @Override
    public void onReferenceChange(final ReferenceChange referenceChange) {

    }

    @Override
    public void onNewObject(final NewObject newObject) {

        if (newObject.getAffectedGlobalId() instanceof InstanceId) {
            return;
        }

        final Optional<CommitMetadata> commitMetadata = newObject.getCommitMetadata();
        commitMetadata.ifPresent(cmd -> {
            final ChangesDTO changesDTO = changesMap.get(cmd.getId().value());
            final String section = getSection(newObject.getAffectedGlobalId());
            final List<ChangeDTO> dtoList = changesDTO.getChanges().remove(section);
            changesDTO.getChanges().put(section + ".new", dtoList);
        });

    }

    @Override
    public void onObjectRemoved(final ObjectRemoved objectRemoved) {

    }

    @Override
    public void onContainerChange(final ContainerChange containerChange) {
        final Optional<CommitMetadata> commitMetadata = containerChange.getCommitMetadata();
        commitMetadata.ifPresent(cmd -> {
            final ChangesDTO changesDTO = changesMap.get(cmd.getId().value());

            containerChange.getValueRemovedChanges().forEach(valueRemoved -> {
                final String section = containerChange.getPropertyName() + "[" + valueRemoved.getIndex() + "]";
                final ChangeDTO changeDTO = new ChangeDTO("", null, null, null, null);
                changesDTO.getChanges().put(section + ".deleted", Collections.singletonList(changeDTO));

            });

        });
    }

    @Override
    public void onSetChange(final SetChange setChange) {

    }

    @Override
    public void onArrayChange(final ArrayChange arrayChange) {

    }

    private String getSection(final GlobalId affectedGlobalId) {

        if (affectedGlobalId instanceof ValueObjectId) {
            final String fragment = ((ValueObjectId) affectedGlobalId).getFragment();

            if (fragment.contains("/")) {

                final String globalIdString = affectedGlobalId.toString();
                String id;
                if (globalIdString.contains("#") && globalIdString.lastIndexOf('/') > globalIdString.lastIndexOf('#')) {
                    id = globalIdString.substring(globalIdString.lastIndexOf('/') + 1, globalIdString.length());
                } else {
                    id = "";
                }

                return fragment.substring(0, fragment.lastIndexOf('/')) + "[" + id + "]";
            } else {
                return fragment;
            }

        } else if (affectedGlobalId instanceof InstanceId) {
            return affectedGlobalId.getTypeName();
        } else {
            return null;
        }
    }

    @Override
    public void onListChange(final ListChange cambio) {
        if (cambio.getAffectedGlobalId() instanceof InstanceId) {
            return;
        }

        final String section = getSection(cambio.getAffectedGlobalId());
        final List<String> added = cambio.getAddedValues().stream().map(Object::toString).collect(toList());
        final List<String> removed = cambio.getRemovedValues().stream().map(Object::toString).collect(toList());

        cambio.getCommitMetadata().ifPresent(commitMetadata -> {
            final ChangeDTO changeDTO = new ChangeDTO(cambio.getPropertyName(), null, null, added, removed);
            changesMap.get(commitMetadata.getId().value()).addChange(section, changeDTO);
        });
    }

    @Override
    public void onMapChange(final MapChange mapChange) {

    }

    @Override
    public List<ChangesDTO> result() {
        changesMap.forEach((k, v) -> {
            if (v.getChanges().isEmpty()) {
                changesMap.remove(k);
            }
        });
        final List<ChangesDTO> changes = new ArrayList<>(changesMap.values());
        changes.sort(CHANGES_DTO_COMPARATOR);
        return changes;
    }
}
