package com.adobe.acs.commons.groovy.extension;

import java.util.Map;

import com.adobe.acs.commons.groovy.context.ScriptContext;

/**
 * Services may implement this interface to supply additional binding values for Groovy script executions.
 */
public interface BindingExtensionProvider {

    /**
     * Get the binding variables for this script execution.  All bindings provided by extension services will be merged
     * prior to script execution.
     *
     * @param scriptContext context for current script execution
     * @return map of binding variables for request
     */
    Map<String, BindingVariable> getBindingVariables(ScriptContext scriptContext);
}
