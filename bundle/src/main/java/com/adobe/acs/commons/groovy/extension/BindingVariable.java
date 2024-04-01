package com.adobe.acs.commons.groovy.extension;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class BindingVariable {

    /** Binding value. */
    private final Object value;

    /** Binding variable type. */
    private final Class<?> type;

    /**
     * Create a new binding variable with the given value.  Type of variable will be derived from it's class.
     *
     * @param value binding value
     */
    public BindingVariable(Object value) {
        this.value = value;
        this.type = value.getClass();
    }

    /**
     * Create a new binding variable with the given value and type.
     *
     * @param value binding value
     * @param type binding variable type
     */
    public BindingVariable(Object value, Class<?> type) {
        this.value = value;
        this.type = type;
    }

    public Object getValue() {
        return value;
    }

    public Class<?> getType() {
        return type;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("value", value)
            .append("type", type)
            .toString();
    }
}
