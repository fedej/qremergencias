package ar.com.utn.proyecto.qremergencias.ws.controller;

import ar.com.utn.proyecto.qremergencias.core.domain.emergency.EmergencyData;
import ar.com.utn.proyecto.qremergencias.core.domain.emergency.GeneralData;
import org.javers.core.Javers;
import org.javers.core.diff.Change;
import org.javers.repository.api.JaversRepository;
import org.javers.repository.jql.JqlQuery;
import org.javers.repository.jql.QueryBuilder;
import org.javers.shadow.Shadow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.management.Query;
import java.util.List;

@RestController
@RequestMapping(value = "/audit")
public class AuditController {

    private final Javers javers;

    @Autowired
    public AuditController(Javers javers) {
        this.javers = javers;
    }

    @RequestMapping("/emergencyData")
    public String getEmergencyDataChanges() {
        //QueryBuilder jqlQueryBuilder = QueryBuilder.byClass(GeneralData.class);
        //QueryBuilder qb = QueryBuilder.byInstanceId("5990648a69d1de6e648652e1",EmergencyData.class);
        QueryBuilder qbEmergencyData = QueryBuilder.byInstanceId("5990648a69d1de6e648652e1",EmergencyData.class);
        JqlQuery jqlQuery = qbEmergencyData.build();
        List<Change> changes = javers.findChanges(jqlQuery);


        QueryBuilder qbPathologies = QueryBuilder.byValueObjectId("5990648a69d1de6e648652e1", EmergencyData.class, "pathologies/2");
        JqlQuery jqlPathologies = qbPathologies.build();

        List<Change> javersChanges = javers.findChanges(jqlPathologies);
        System.out.println(javers.getJsonConverter().toJson(javersChanges));

        return javers.getJsonConverter().toJson(changes);
    }
}

