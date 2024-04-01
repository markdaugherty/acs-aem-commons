package com.adobe.acs.commons.groovy.constants;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.net.MediaType;

public final class GroovyScriptConstants {

//    public static final String SERVICE_USER_NAME = "groovy-console-service";

    public static final String SUBSERVICE_NAME = "groovy";

    public static final String PATH_GROOVY_ROOT = "/var/acs-commons/groovy";

    public static final String PATH_SCRIPTS_FOLDER = PATH_GROOVY_ROOT + "/scripts";

    public static final String EXTENSION_GROOVY = ".groovy";

    public static final String FORMAT_RUNNING_TIME = "HH:mm:ss.SSS";

    public static final String TIME_ZONE_RUNNING_TIME = "GMT";

    public static final String DATE_FORMAT_DISPLAY = "yyyy-MM-dd HH:mm:ss";

    public static final String DATE_FORMAT_FILE_NAME = "yyyy-MM-dd'T'HHmmss";

    // request parameters/properties

    public static final String FILE_NAME = "fileName";

    public static final String SCRIPT_PATH = "scriptPath";

    public static final String SCRIPT_PATHS = "scriptPaths";

    public static final String SCRIPT = "script";

    public static final String USER_ID = "userId";

    public static final String START_DATE = "startDate";

    public static final String END_DATE = "endDate";

    public static final String DATA = "data";

    public static final String EMAIL_TO = "emailTo";

    public static final String DATE_CREATED = "dateCreated";

    public static final String RESULT = "result";

    public static final String OUTPUT = "output";

    public static final String MEDIA_TYPE = "mediaType";

    public static final String SUCCESS = "success";

    public static final String EXCEPTION_STACK_TRACE = "exceptionStackTrace";

    public static final String RUNNING_TIME = "runningTime";

    // audit

    public static final String AUDIT_NODE_NAME = "audit";

    public static final String AUDIT_RECORD_NODE_PREFIX = "record";

    public static final String AUDIT_PATH = PATH_GROOVY_ROOT + "/" + AUDIT_NODE_NAME;

    public static final Map<String, String> MEDIA_TYPE_EXTENSIONS = ImmutableMap.of(
        (MediaType.CSV_UTF_8.withoutParameters().toString()), "csv",
        (MediaType.PLAIN_TEXT_UTF_8.withoutParameters().toString()), "txt",
        (MediaType.HTML_UTF_8.withoutParameters().toString()), "html",
        (MediaType.XML_UTF_8.withoutParameters().toString()), "xml"
    );

    private GroovyScriptConstants() {

    }
}
