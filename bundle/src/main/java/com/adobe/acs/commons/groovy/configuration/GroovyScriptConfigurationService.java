package com.adobe.acs.commons.groovy.configuration;

import java.util.Set;

import org.apache.sling.api.SlingHttpServletRequest;

public interface GroovyScriptConfigurationService {

    /**
     * Check if the current user has permission to execute Groovy scripts in the console.
     *
     * @param request current execution request
     * @return true if user has permission
     */
    boolean hasPermission(SlingHttpServletRequest request);

    /**
     * Check if email is enabled.
     *
     * @return true if email is enabled
     */
    boolean isEmailEnabled();

    /**
     * Get the set of configured email recipients for Groovy script notifications.
     *
     * @return set of email addresses
     */
    Set<String> getEmailRecipients();

    /**
     * Check if auditing is disabled.
     *
     * @return true if auditing is disabled
     */
    boolean isAuditDisabled();

    /**
     * Check if all audit records should be displayed in the History panel.  By default,
     * only records for the current user will be displayed.
     *
     * @return if true, display all audit records
     */
    boolean isDisplayAllAuditRecords();

    /**
     * Get the thread timeout value in seconds.  Scripts will be interrupted when the timeout value is reached.  If zero, no timeout will be enforced.
     *
     * @return thread timeout
     */
    long getThreadTimeout();

    Set<String> getAllowedServices();
}
