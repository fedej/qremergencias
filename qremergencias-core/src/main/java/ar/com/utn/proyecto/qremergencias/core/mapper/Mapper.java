package ar.com.utn.proyecto.qremergencias.core.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class Mapper<S, T> implements Function<S, T> {

    private Function<S, T> constructor;

    private final List<BiConsumer<S, T>> fields = new ArrayList<>();

    @Override
    public T apply(final S source) {
        return apply(source, constructor.apply(source));
    }

    public T apply(final S source, final T target) {
        fields.forEach(f -> f.accept(source, target));
        return target;
    }

    public <O> Mapper<S, T> fields(final Function<S, O> getter, final BiConsumer<T, O> setter) {
        Objects.requireNonNull(getter);
        Objects.requireNonNull(setter);

        fields.add((source, target) -> {
            final O value = getter.apply(source);
            if (Objects.nonNull(value)) {
                setter.accept(target, value);
            }
        });

        return this;
    }

    public <O1, O2> Mapper<S, T> fields(final Function<S, O1> getter,
                                        final BiConsumer<T, O2> setter,
                                        final Function<O1, O2> converter) {
        Objects.requireNonNull(getter);
        Objects.requireNonNull(setter);
        Objects.requireNonNull(converter);

        fields(getter.andThen(value -> value == null ? null : converter.apply(value)), setter);
        return this;
    }

    public Mapper<S, T> constructor(final Supplier<T> constructor) {
        this.constructor = (dontuse) -> constructor.get();
        return this;
    }

    public Mapper<S, T> constructor(final Function<S, T> constructor) {
        this.constructor = constructor;
        return this;
    }

    public <O> Mapper<S, T> constructor(final Function<O, T> construct,
                                        final Function<S, O> param1) {
        this.constructor = (S source) -> construct.apply(param1.apply(source));
        return this;
    }

    public <O1, O2> Mapper<S, T> constructor(final BiFunction<O1, O2, T> construct,
                                             final Function<S, O1> param1,
                                             final Function<S, O2> param2) {
        this.constructor = (S source) ->
            construct.apply(param1.apply(source), param2.apply(source));
        return this;
    }

    public static <S, T> Mapper<S, T> mapping(final Class<S> source, final Class<T> target) {
        return new Mapper<>();
    }

}
