package ar.com.utn.proyecto.qremergencias.core.mapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public final class Converters {

    public static final Function<String, String> UPPERCASE = String::toUpperCase;
    public static final Function<String, String> LOWERCASE = String::toLowerCase;
    public static final Function<?, String> TO_STRING = Objects::toString;

    public static <S, T> Function<List<S>, List<T>> listConverter(final Function<S, T> mapper) {
        return list -> list.stream().map(mapper).collect(toList());
    }

    public static <S, T> Function<Set<S>, Set<T>> setConverter(final Function<S, T> mapper) {
        return set -> set.stream().map(mapper).collect(toSet());
    }

    public static <S, T> Function<Set<S>, List<T>> setToListConverter(final Function<S, T> mapper) {
        return set -> set.stream().map(mapper).collect(toList());
    }

    public static <S, T> Function<List<S>, Set<T>> listToSetConverter(final Function<S, T> mapper) {
        return list -> list.stream().map(mapper).collect(toSet());
    }

    public static Function<LocalDate, LocalDateTime> addTimeConverter(final LocalTime localTime) {
        return localDate -> LocalDateTime.of(localDate, localTime);
    }

    public static Function<LocalDateTime, LocalDate> localDateConverter() {
        return LocalDateTime::toLocalDate;
    }
    
    private Converters() {

    }

}
