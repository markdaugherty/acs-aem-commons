package com.adobe.acs.commons.groovy.services.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.lang3.time.StopWatch;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer;
import org.codehaus.groovy.control.customizers.CompilationCustomizer;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.acs.commons.groovy.audit.GroovyScriptAuditService;
import com.adobe.acs.commons.groovy.configuration.GroovyScriptConfigurationService;
import com.adobe.acs.commons.groovy.context.ScriptContext;
import com.adobe.acs.commons.groovy.context.ScriptData;
import com.adobe.acs.commons.groovy.exception.GroovyException;
import com.adobe.acs.commons.groovy.extension.BindingVariable;
import com.adobe.acs.commons.groovy.extension.GroovyScriptExtensionService;
import com.adobe.acs.commons.groovy.response.RunScriptResponse;
import com.adobe.acs.commons.groovy.response.SaveScriptResponse;
import com.adobe.acs.commons.groovy.response.impl.DefaultRunScriptResponse;
import com.adobe.acs.commons.groovy.services.GroovyScriptExecutor;
import com.adobe.acs.commons.groovy.utils.GroovyScriptUtils;
import com.google.common.collect.ImmutableMap;
import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import groovy.transform.TimedInterrupt;

import static com.adobe.acs.commons.groovy.constants.GroovyScriptConstants.FORMAT_RUNNING_TIME;
import static com.adobe.acs.commons.groovy.constants.GroovyScriptConstants.TIME_ZONE_RUNNING_TIME;

@Component(service = GroovyScriptExecutor.class, immediate = true)
public final class GroovyScriptExecutorImpl implements GroovyScriptExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(GroovyScriptExecutorImpl.class);

    @Reference
    private GroovyScriptConfigurationService configurationService;

//    private volatile List<NotificationService> notificationServices = new CopyOnWriteArrayList<>();

    @Reference
    private GroovyScriptAuditService auditService;

    @Reference
    private GroovyScriptExtensionService extensionService;

    @Override
    public RunScriptResponse runScript(final ScriptContext scriptContext) throws GroovyException {
        final Binding binding = getBinding(scriptContext);

        final StopWatch stopWatch = StopWatch.createStarted();

        RunScriptResponse runScriptResponse;

        try {
            final Script script = new GroovyShell(binding, getConfiguration()).parse(scriptContext.getScript());

            for (final Closure<?> scriptMetaClass : extensionService.getScriptMetaClasses(scriptContext)) {
                GroovyScriptUtils.setMetaClass(script, scriptMetaClass);
            }

            final String result = (String) script.run();

            runScriptResponse = DefaultRunScriptResponse.fromResult(scriptContext, result,
                getScriptOutput(scriptContext), getRunningTime(stopWatch));

            LOG.debug("Script execution completed.");

            auditAndNotify(runScriptResponse);
        } catch (MultipleCompilationErrorsException e) {
            LOG.error("Script compilation error", e);

            runScriptResponse = DefaultRunScriptResponse.fromException(scriptContext,
                getScriptOutput(scriptContext), null, e);
        } catch (Throwable t) {
            LOG.error("Error running Groovy script", t);

            runScriptResponse = DefaultRunScriptResponse.fromException(scriptContext,
                getScriptOutput(scriptContext), getRunningTime(stopWatch), t);

            auditAndNotify(runScriptResponse);
        }

        return runScriptResponse;
    }

    @Override
    public SaveScriptResponse saveScript(final ScriptData scriptData) {
        return null;
    }

    // internals

    private void auditAndNotify(final RunScriptResponse response) throws GroovyException {
        if (!configurationService.isAuditDisabled()) {
            auditService.createAuditRecord(response);
        }

//        notificationServices.each { notificationService ->
//            notificationService.notify(response)
//        }
    }

    private Binding getBinding(final ScriptContext scriptContext) {
        Binding binding = new Binding();

        for (final Map.Entry<String, BindingVariable> entry : extensionService.getBindingVariables(scriptContext).entrySet()) {
            binding.setVariable(entry.getKey(), entry.getValue().getValue());
        }

        return binding;
    }

    private CompilerConfiguration getConfiguration() {
        final CompilerConfiguration configuration = new CompilerConfiguration();

        if (configurationService.getThreadTimeout() > 0) {
            // add timed interrupt using configured timeout value
            configuration.addCompilationCustomizers(new ASTTransformationCustomizer(
                ImmutableMap.of("value", configurationService.getThreadTimeout()), TimedInterrupt.class));
        }

        return configuration.addCompilationCustomizers(extensionService.getCompilationCustomizers()
            .toArray(new CompilationCustomizer[0]));
    }

    private String getRunningTime(final StopWatch stopWatch) {
        final SimpleDateFormat dateFormat = new SimpleDateFormat(FORMAT_RUNNING_TIME);

        dateFormat.setTimeZone(TimeZone.getTimeZone(TIME_ZONE_RUNNING_TIME));

        final Date date = new Date();

        date.setTime(stopWatch.getTime());

        return dateFormat.format(date);
    }

    private String getScriptOutput(final ScriptContext scriptContext) throws GroovyException {
        final String output;

        try (final ByteArrayOutputStream outputStream = scriptContext.getOutputStream()) {
            output = outputStream.toString(StandardCharsets.UTF_8.name());
        } catch (IOException e) {
            throw new GroovyException("Error getting script output", e);
        }

        return output;
    }
}
