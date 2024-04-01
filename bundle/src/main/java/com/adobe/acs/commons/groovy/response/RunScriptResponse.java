package com.adobe.acs.commons.groovy.response;

import java.util.Calendar;

import javax.annotation.Nullable;

public interface RunScriptResponse {

    /**
     * Get the date of script execution.
     *
     * @return execution date
     */
    Calendar getDate();

    String getScript();

    @Nullable
    String getData();

    @Nullable
    String getResult();

    @Nullable
    String getOutput();

    /**
     * @return true if script execution was successful, false if an exception occurred
     */
    boolean isSuccess();

    /**
     * Get the exception stack trace in case of script failure.
     *
     * @return exception stack trace
     */
    @Nullable
    String getExceptionStackTrace();

    @Nullable
    String getRunningTime();

    String getUserId();

    String getMediaType();

    String getOutputFileName();
}
