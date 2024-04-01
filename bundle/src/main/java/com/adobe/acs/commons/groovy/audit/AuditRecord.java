package com.adobe.acs.commons.groovy.audit;

import javax.annotation.Nullable;

import com.adobe.acs.commons.groovy.response.RunScriptResponse;

public interface AuditRecord extends RunScriptResponse {

    String getDownloadUrl();

    String getRelativePath();

    @Nullable
    String getException();
}
