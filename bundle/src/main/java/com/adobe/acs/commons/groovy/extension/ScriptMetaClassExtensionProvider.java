package com.adobe.acs.commons.groovy.extension;

import com.adobe.acs.commons.groovy.context.ScriptContext;

import groovy.lang.Closure;

/**
 * Services may implement this interface to supply additional metamethods to apply to the <code>Script</code>
 * metaclass.
 */
public interface ScriptMetaClassExtensionProvider {

    /**
     * Get a closure to register a metaclass for the script to be executed.
     *
     * @param scriptContext current script execution context
     * @return a closure containing meta-methods to register for scripts
     */
    Closure<?> getScriptMetaClass(ScriptContext scriptContext);
}
