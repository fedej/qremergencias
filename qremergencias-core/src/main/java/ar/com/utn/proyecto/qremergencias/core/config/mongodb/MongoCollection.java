package ar.com.utn.proyecto.qremergencias.core.config.mongodb;

import lombok.Data;

import java.util.List;

@Data
public class MongoCollection {
    private String name;
    private List<MongoObject> objects;
}
