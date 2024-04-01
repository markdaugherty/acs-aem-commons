package com.adobe.acs.commons.groovy.services;

import com.adobe.acs.commons.groovy.exception.GroovyException;
import com.adobe.acs.commons.groovy.context.ScriptContext;
import com.adobe.acs.commons.groovy.context.ScriptData;
import com.adobe.acs.commons.groovy.response.RunScriptResponse;
import com.adobe.acs.commons.groovy.response.SaveScriptResponse;

public interface GroovyScriptExecutor {

    /**
     * Run a Groovy script with the given script context.
     *
     * @param scriptContext script context
     * @return response containing script output
     */
    RunScriptResponse runScript(ScriptContext scriptContext) throws GroovyException;

    /**
     * Save a Groovy script with the file name and content provided in the given script data.
     *
     * @param scriptData script data
     * @return response containing the name of the saved script
     */
    SaveScriptResponse saveScript(ScriptData scriptData);
}
