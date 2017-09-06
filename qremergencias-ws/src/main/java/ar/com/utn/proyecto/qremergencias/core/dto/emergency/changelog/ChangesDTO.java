package ar.com.utn.proyecto.qremergencias.core.dto.emergency.changelog;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class ChangesDTO {
    private final String id;
    private final LocalDateTime date;
    private final String author;
    private final Map<String, List<ChangeDTO>> changes;
}
