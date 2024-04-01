package com.adobe.acs.commons.groovy.context.impl;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ResourceResolver;

import com.adobe.acs.commons.groovy.context.ScriptData;

import static com.adobe.acs.commons.groovy.constants.GroovyScriptConstants.EXTENSION_GROOVY;
import static com.adobe.acs.commons.groovy.constants.GroovyScriptConstants.FILE_NAME;
import static com.adobe.acs.commons.groovy.constants.GroovyScriptConstants.SCRIPT;

public final class RequestScriptData implements ScriptData {

    private final SlingHttpServletRequest request;

    public RequestScriptData(final SlingHttpServletRequest request) {
        this.request = request;
    }

    @Override
    public ResourceResolver getResourceResolver() {
        return request.getResourceResolver();
    }

    @Override
    public String getFileName() {
        final String name = request.getParameter(FILE_NAME);

        return name.endsWith(EXTENSION_GROOVY) ? name : name + EXTENSION_GROOVY;
    }

    @Override
    public String getScript() {
        return request.getParameter(SCRIPT);
    }
}
