package com.adobe.acs.commons.groovy.extension;

import java.util.List;

import com.adobe.acs.commons.groovy.context.ScriptContext;

import groovy.lang.Closure;

public interface GroovyScriptExtensionService extends BindingExtensionProvider, CompilationCustomizerExtensionProvider,
    StarImportExtensionProvider {

    /**
     * Get a list of all script metaclass closures for bound extensions.
     *
     * @param scriptContext current script execution context
     * @return list of metaclass closures
     */
    List<Closure<?>> getScriptMetaClasses(ScriptContext scriptContext);
}
