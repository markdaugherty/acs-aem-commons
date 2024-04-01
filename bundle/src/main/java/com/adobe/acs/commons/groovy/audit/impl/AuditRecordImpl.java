package com.adobe.acs.commons.groovy.audit.impl;

import java.util.Calendar;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;

import com.adobe.acs.commons.groovy.audit.AuditRecord;
import com.adobe.acs.commons.groovy.response.RunScriptResponse;
import com.adobe.acs.commons.groovy.response.impl.DefaultRunScriptResponse;
import com.day.text.Text;

public final class AuditRecordImpl implements AuditRecord {

    private static final int DEPTH_RELATIVE_PATH = 3;

    private final Resource resource;

    private final RunScriptResponse delegate;

    public AuditRecordImpl(final Resource resource) {
        this.resource = resource;

        delegate = DefaultRunScriptResponse.fromAuditRecordResource(resource);
    }

    @Override
    public String getDownloadUrl() {
        return null;
    }

    @Override
    public String getRelativePath() {
        return StringUtils.removeStart(resource.getPath(), Text.getAbsoluteParent(resource.getPath(),
            DEPTH_RELATIVE_PATH)).substring(1);
    }

    @Override
    public String getException() {
        String exception = "";

//        if (StringUtils.isNotEmpty(delegate.getExceptionStackTrace())) {
//            def firstLine = exceptionStackTrace.readLines().first();
//
//            if (firstLine.contains(":")) {
//                exception = firstLine.substring(0, firstLine.indexOf(":"))
//            } else {
//                exception = firstLine
//            }
//        }

        return exception;
    }

    @Override
    public Calendar getDate() {
        return delegate.getDate();
    }

    @Override
    public String getScript() {
        return delegate.getScript();
    }

    @Override
    public String getData() {
        return delegate.getData();
    }

    @Override
    public String getResult() {
        return delegate.getResult();
    }

    @Override
    public String getOutput() {
        return delegate.getOutput();
    }

    @Override
    public boolean isSuccess() {
        return delegate.isSuccess();
    }

    @Override
    public String getExceptionStackTrace() {
        return delegate.getExceptionStackTrace();
    }

    @Override
    public String getRunningTime() {
        return delegate.getRunningTime();
    }

    @Override
    public String getUserId() {
        return delegate.getUserId();
    }

    @Override
    public String getMediaType() {
        return delegate.getMediaType();
    }

    @Override
    public String getOutputFileName() {
        return delegate.getOutputFileName();
    }
}
