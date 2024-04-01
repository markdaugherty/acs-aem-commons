package com.adobe.acs.commons.groovy.servlets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.acs.commons.groovy.context.ScriptContext;
import com.adobe.acs.commons.groovy.exception.GroovyException;
import com.adobe.acs.commons.groovy.context.impl.RequestScriptContext;
import com.adobe.acs.commons.groovy.configuration.GroovyScriptConfigurationService;
import com.adobe.acs.commons.groovy.response.RunScriptResponse;
import com.adobe.acs.commons.groovy.services.GroovyScriptExecutor;
import com.day.cq.commons.jcr.JcrConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.MediaType;

import static com.adobe.acs.commons.groovy.constants.GroovyScriptConstants.SCRIPT_PATH;

@SlingServlet(paths = "/bin/acs-commons/groovy/post")
public final class GroovyScriptExecutionPostServlet extends SlingAllMethodsServlet {

    private static final Logger LOG = LoggerFactory.getLogger(GroovyScriptExecutionPostServlet.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Reference
    private GroovyScriptConfigurationService configurationService;

    @Reference
    private GroovyScriptExecutor executionService;

    @Override
    protected void doPost(@NotNull final SlingHttpServletRequest request,
        @NotNull final SlingHttpServletResponse response) throws ServletException, IOException {
        response.setContentType(MediaType.JSON_UTF_8.withoutParameters().toString());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        if (configurationService.hasPermission(request)) {
            final String[] scriptPaths = request.getParameterValues(SCRIPT_PATH);

            try {
                if (scriptPaths == null) {
                    throw new ServletException("Script paths not provided.");
                }

                final List<RunScriptResponse> runScriptResponses = new ArrayList<>();

                for (final String script : getScripts(request, scriptPaths)) {
                    runScriptResponses.add(runScript(request, response, script));
                }

                MAPPER.writeValue(response.getWriter(), runScriptResponses);
            } catch (ServletException e) {
                LOG.error("Error running Groovy scripts: " + e.getMessage());

                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            } catch (GroovyException e) {
                LOG.error("Error running Groovy scripts", e);

                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } else {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        }
    }

    private List<String> getScripts(final SlingHttpServletRequest request, final String[] scriptPaths)
        throws IOException, ServletException {
        final List<String> scripts = new ArrayList<>();

        for (final String scriptPath : scriptPaths) {
            scripts.add(loadScript(request, scriptPath));
        }

        return scripts;
    }

    private RunScriptResponse runScript(final SlingHttpServletRequest request,
        final SlingHttpServletResponse response, final String script) throws IOException, GroovyException {
        try (final ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            final ScriptContext scriptContext = getScriptContext(request, response,
                outputStream, script);

            return executionService.runScript(scriptContext);
        }
    }

    private ScriptContext getScriptContext(final SlingHttpServletRequest request,
        final SlingHttpServletResponse response, final ByteArrayOutputStream outputStream,
        final String script) throws UnsupportedEncodingException {
        final PrintStream printStream = new PrintStream(outputStream, true, StandardCharsets.UTF_8.name());

        return new RequestScriptContext(request, response, outputStream, printStream, script);
    }

    private String loadScript(final SlingHttpServletRequest request, final String scriptPath)
        throws IOException, ServletException {
        final Resource scriptResource = request.getResourceResolver().getResource(scriptPath + "/"
            + JcrConstants.JCR_CONTENT);

        if (scriptResource == null) {
            throw new ServletException("Groovy script not found for path: " + scriptPath);
        }

        final String script;

        try (final InputStream stream = scriptResource.getValueMap().get(JcrConstants.JCR_DATA,
            InputStream.class)) {
            if (stream == null) {
                throw new ServletException("Groovy script data not found for path: " + scriptPath);
            } else {
                script = IOUtils.toString(stream, StandardCharsets.UTF_8);
            }
        }

        return script;
    }
}
