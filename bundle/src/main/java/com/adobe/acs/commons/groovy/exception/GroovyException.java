package com.adobe.acs.commons.groovy.exception;

public final class GroovyException extends Exception {

    public GroovyException(final String message) {
        super(message);
    }

    public GroovyException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
