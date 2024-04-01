package com.adobe.acs.commons.groovy.context.impl;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;

import com.adobe.acs.commons.groovy.context.ServletScriptContext;

import static com.adobe.acs.commons.groovy.constants.GroovyScriptConstants.DATA;

public final class RequestScriptContext implements ServletScriptContext {

    private final SlingHttpServletRequest request;

    private final SlingHttpServletResponse response;

    private final ByteArrayOutputStream outputStream;

    private final PrintStream printStream;

    private final String script;

    public RequestScriptContext(final SlingHttpServletRequest request, final SlingHttpServletResponse response,
        final ByteArrayOutputStream outputStream, final PrintStream printStream, final String script) {
        this.request = request;
        this.response = response;
        this.outputStream = outputStream;
        this.printStream = printStream;
        this.script = script;
    }

    @Override
    public ResourceResolver getResourceResolver() {
        return request.getResourceResolver();
    }

    @Override
    public ByteArrayOutputStream getOutputStream() {
        return outputStream;
    }

    @Override
    public PrintStream getPrintStream() {
        return printStream;
    }

    @Override
    public String getScript() {
        return script;
    }

    @Override
    public String getData() {
        return request.getParameter(DATA);
    }

    @Override
    public String getUserId() {
        return request.getResourceResolver().getUserID();
    }

    @Override
    public SlingHttpServletRequest getRequest() {
        return request;
    }

    @Override
    public SlingHttpServletResponse getResponse() {
        return response;
    }
}
