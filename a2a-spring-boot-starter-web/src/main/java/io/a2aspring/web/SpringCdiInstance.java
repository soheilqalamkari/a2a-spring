package io.a2aspring.web;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Iterator;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.UnsatisfiedResolutionException;
import jakarta.enterprise.util.TypeLiteral;

/** Minimal CDI Instance bridge for SDK extension points constructed by Spring. */
final class SpringCdiInstance<T> implements Instance<T> {
    private final T value;
    private final boolean present;

    private SpringCdiInstance(T value, boolean present) {
        this.value = value;
        this.present = present;
    }

    static <T> SpringCdiInstance<T> empty() {
        return new SpringCdiInstance<>(null, false);
    }

    static <T> SpringCdiInstance<T> of(T value) {
        return new SpringCdiInstance<>(value, true);
    }

    @Override public T get() {
        if (!present) throw new UnsatisfiedResolutionException();
        return value;
    }

    @Override
    public boolean isUnsatisfied() {
        return !present;
    }
    @Override
    public boolean isAmbiguous() {
        return false;
    }
    @Override
    public void destroy(T instance) {

    }
    @Override
    public Iterator<T> iterator() {
        return present ? Collections.singleton(value).iterator() : Collections.emptyIterator();
    }
    @Override
    public Instance<T> select(Annotation... qualifiers) {
        return this;
    }
    @Override
    public <U extends T> Instance<U> select(Class<U> subtype, Annotation... qualifiers) {
        return empty();
    }
    @Override
    public <U extends T> Instance<U> select(TypeLiteral<U> subtype, Annotation... qualifiers) {
        return empty();
    }
    @Override
    public Handle<T> getHandle() {
        throw new UnsatisfiedResolutionException();
    }
    @Override
    public Iterable<? extends Handle<T>> handles() {
        return Collections.emptyList();
    }
}
