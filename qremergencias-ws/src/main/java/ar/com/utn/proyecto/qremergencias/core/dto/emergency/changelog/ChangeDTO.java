package ar.com.utn.proyecto.qremergencias.core.dto.emergency.changelog;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@ToString(of = {"property", "oldValue", "newValue"})
@RequiredArgsConstructor
public class ChangeDTO {
    private final String property;
    private final Object oldValue;
    private final Object newValue;
    private final List<String> added;
    private final List<String> removed;
}
