package ar.com.utn.proyecto.qremergencias.ws.service;

import ar.com.utn.proyecto.qremergencias.changes.TableChangeProcessor;
import ar.com.utn.proyecto.qremergencias.core.domain.UserFront;
import ar.com.utn.proyecto.qremergencias.core.domain.emergency.EmergencyData;
import ar.com.utn.proyecto.qremergencias.core.dto.FilterDTO;
import ar.com.utn.proyecto.qremergencias.core.dto.emergency.changelog.ChangesDTO;
import ar.com.utn.proyecto.qremergencias.core.repository.EmergencyDataRepository;
import org.javers.core.Javers;
import org.javers.core.diff.Change;
import org.javers.repository.jql.QueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ChangesService {

    private final Javers javers;
    private final EmergencyDataRepository repository;

    @Autowired
    public ChangesService(final Javers javers, final EmergencyDataRepository repository) {
        this.javers = javers;
        this.repository = repository;
    }

    public PageImpl<ChangesDTO> getEmergencyDataChanges(final UserFront user, final Pageable page,
                                                        final FilterDTO filter) {
        final Optional<EmergencyData> optionalData = repository.findByUser(user);

        if (!optionalData.isPresent()) {
            return new PageImpl<>(Collections.emptyList());
        }

        final EmergencyData ed = optionalData.get();
        final QueryBuilder qbPathologies =
                QueryBuilder.byInstanceId(ed.getId(), EmergencyData.class)
                        .withNewObjectChanges()
                        .withChildValueObjects();

        if (filter != null) {
            if (filter.getFrom() != null) {
                qbPathologies.from(filter.getFrom());
            }
            if (filter.getTo() != null) {
                qbPathologies.to(filter.getTo());
            }
            if (filter.getText() != null) {
                qbPathologies.byAuthor(filter.getText());
            }
        }

        final List<Change> changes = javers.findChanges(qbPathologies.build());
        final List<ChangesDTO> changesList = javers.processChangeList(changes, new TableChangeProcessor());
        return new PageImpl<>(paginate(changesList, page), page, changesList.size());
    }

    private <T> List<T> paginate(final List<T> collection, final Pageable page) {
        return collection.stream()
                .skip(page.getOffset())
                .limit(page.getPageSize())
                .collect(Collectors.toList());
    }

}
