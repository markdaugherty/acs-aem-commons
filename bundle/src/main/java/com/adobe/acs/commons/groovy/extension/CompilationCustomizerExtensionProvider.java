package com.adobe.acs.commons.groovy.extension;

import java.util.List;

import org.codehaus.groovy.control.customizers.CompilationCustomizer;

/**
 * Services may implement this interface to customize the compiler configuration for Groovy script execution.
 */
public interface CompilationCustomizerExtensionProvider {

    /**
     * Get a list of compilation customizers for Groovy script execution.
     *
     * @return list of compilation customizers
     */
    List<CompilationCustomizer> getCompilationCustomizers();
}
