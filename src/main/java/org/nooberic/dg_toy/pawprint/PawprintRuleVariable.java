package org.nooberic.dg_toy.pawprint;

import java.util.Objects;
import java.util.function.Predicate;

public final class PawprintRuleVariable<T> {
    private final String key;
    private final String displayName;
    private final Class<T> type;
    private final T defaultValue;
    private final Predicate<T> validator;

    public PawprintRuleVariable(String key, String displayName, Class<T> type, T defaultValue, Predicate<T> validator) {
        this.key = Objects.requireNonNull(key, "key");
        this.displayName = Objects.requireNonNull(displayName, "displayName");
        this.type = Objects.requireNonNull(type, "type");
        this.defaultValue = defaultValue;
        this.validator = validator == null ? value -> true : validator;
    }

    public String key() {
        return this.key;
    }

    public Class<T> type() {
        return this.type;
    }

    public String displayName() {
        return this.displayName;
    }

    public T defaultValue() {
        return this.defaultValue;
    }

    public boolean isValid(T value) {
        return value != null && this.validator.test(value);
    }
}