package com.adobe.acs.commons.groovy.response.impl;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;

import com.adobe.acs.commons.groovy.context.ScriptContext;
import com.adobe.acs.commons.groovy.response.RunScriptResponse;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.text.Text;
import com.google.common.net.MediaType;

import static com.adobe.acs.commons.groovy.constants.GroovyScriptConstants.DATA;
import static com.adobe.acs.commons.groovy.constants.GroovyScriptConstants.DATE_FORMAT_FILE_NAME;
import static com.adobe.acs.commons.groovy.constants.GroovyScriptConstants.EXCEPTION_STACK_TRACE;
import static com.adobe.acs.commons.groovy.constants.GroovyScriptConstants.MEDIA_TYPE_EXTENSIONS;
import static com.adobe.acs.commons.groovy.constants.GroovyScriptConstants.OUTPUT;
import static com.adobe.acs.commons.groovy.constants.GroovyScriptConstants.RESULT;
import static com.adobe.acs.commons.groovy.constants.GroovyScriptConstants.RUNNING_TIME;
import static com.adobe.acs.commons.groovy.constants.GroovyScriptConstants.SCRIPT;
import static org.osgi.jmx.framework.FrameworkMBean.SUCCESS;

public final class DefaultRunScriptResponse implements RunScriptResponse {

    public static RunScriptResponse fromResult(final ScriptContext scriptContext, final String result,
        final String output, final String runningTime) {
        return new DefaultRunScriptResponse(Calendar.getInstance(), scriptContext.getScript(),
            scriptContext.getData(), result, output, true, null, runningTime, scriptContext.getUserId());
    }

    public static RunScriptResponse fromException(final ScriptContext scriptContext, final String output,
        final String runningTime, final Throwable throwable) {
        final String exceptionStackTrace = ExceptionUtils.getStackTrace(throwable);

        return new DefaultRunScriptResponse(Calendar.getInstance(), scriptContext.getScript(),
            scriptContext.getData(), null, output, false, exceptionStackTrace, runningTime,
            scriptContext.getUserId());
    }

    public static RunScriptResponse fromAuditRecordResource(final Resource resource) {
        final ValueMap properties = resource.getValueMap();

        final String userIdResourcePath = ResourceUtil.getParent(resource.getPath(), LEVEL_USERID);
        final String userId = Text.getName(userIdResourcePath);

        return new DefaultRunScriptResponse(properties.get(JcrConstants.JCR_CREATED, Calendar.class),
            properties.get(SCRIPT, String.class),
            properties.get(DATA, String.class),
            properties.get(RESULT, String.class),
            properties.get(OUTPUT, String.class),
            properties.get(SUCCESS, false),
            properties.get(EXCEPTION_STACK_TRACE, String.class),
            properties.get(RUNNING_TIME, String.class), userId);
    }

    private static final int LEVEL_USERID = 4;

    private final Calendar date;

    private final String script;

    private final String data;

    private final String result;

    private final String output;

    private final boolean success;

    private final String exceptionStackTrace;

    private final String runningTime;

    private final String userId;

    private DefaultRunScriptResponse(final Calendar date, final String script, final String data,
        final String result, final String output, final boolean success, final String exceptionStackTrace,
        final String runningTime, final String userId) {
        this.date = date;
        this.script = script;
        this.data = data;
        this.result = result;
        this.output = output;
        this.success = success;
        this.exceptionStackTrace = exceptionStackTrace;
        this.runningTime = runningTime;
        this.userId = userId;
    }

    @Override
    public Calendar getDate() {
        return date;
    }

    @Override
    public String getScript() {
        return script;
    }

    @Override
    public String getData() {
        return data;
    }

    @Override
    public String getResult() {
        return result;
    }

    @Override
    public String getOutput() {
        return output;
    }

    @Override
    public boolean isSuccess() {
        return success;
    }

    @Override
    public String getExceptionStackTrace() {
        return exceptionStackTrace;
    }

    @Override
    public String getRunningTime() {
        return runningTime;
    }

    @Override
    public String getUserId() {
        return userId;
    }

    @Override
    public String getMediaType() {
        return MediaType.PLAIN_TEXT_UTF_8.withoutParameters().toString();
    }

    @Override
    public String getOutputFileName() {
        return new StringBuilder()
            .append("output-")
            .append(new SimpleDateFormat(DATE_FORMAT_FILE_NAME).format(date.getTime()))
            .append(".")
            .append(MEDIA_TYPE_EXTENSIONS.get(getMediaType()))
            .toString();
    }


}
