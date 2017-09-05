package ar.com.utn.proyecto.qremergencias.core.dto.emergency.changelog;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@RequiredArgsConstructor
@EqualsAndHashCode(of = {"id", "date", "author"})
@ToString(of = {"id", "date", "author"})
public class ChangeDTOId {
    private final String id;
    private final LocalDateTime date;
    private final String author;
}
