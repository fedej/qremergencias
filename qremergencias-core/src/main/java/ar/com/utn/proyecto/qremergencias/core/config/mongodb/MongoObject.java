package ar.com.utn.proyecto.qremergencias.core.config.mongodb;

import lombok.Data;

import java.util.Map;

@Data
public class MongoObject {
    private Map<String, String> relationships;
    private Object data;
}
