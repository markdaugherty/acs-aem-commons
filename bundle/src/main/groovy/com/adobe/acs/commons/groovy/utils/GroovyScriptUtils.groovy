package com.adobe.acs.commons.groovy.utils

final class GroovyScriptUtils {

    static void setMetaClass(Script script, Closure closure) {
        script.metaClass(closure)
    }

    private GroovyScriptUtils() {

    }
}
