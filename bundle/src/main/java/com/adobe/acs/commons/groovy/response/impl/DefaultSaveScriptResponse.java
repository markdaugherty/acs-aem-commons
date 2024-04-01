package com.adobe.acs.commons.groovy.response.impl;

import com.adobe.acs.commons.groovy.response.SaveScriptResponse;

public final class DefaultSaveScriptResponse implements SaveScriptResponse {

    private final String scriptName;

    public DefaultSaveScriptResponse(final String scriptName) {
        this.scriptName = scriptName;
    }

    @Override
    public String getScriptName() {
        return scriptName;
    }
}
