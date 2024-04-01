package com.adobe.acs.commons.groovy.configuration.impl;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.jcr.RepositoryException;

import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.acs.commons.groovy.configuration.GroovyScriptConfigurationService;
import com.google.common.collect.ImmutableSet;

import static com.adobe.acs.commons.groovy.constants.GroovyScriptConstants.SUBSERVICE_NAME;

@Component(service = GroovyScriptConfigurationService.class, immediate = true)
@Designate(ocd = DefaultGroovyScriptConfigurationService.Configuration.class)
public final class DefaultGroovyScriptConfigurationService implements GroovyScriptConfigurationService {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultGroovyScriptConfigurationService.class);

    private static final Map<String, Object> AUTH_INFO = Collections.singletonMap(
        ResourceResolverFactory.SUBSERVICE, SUBSERVICE_NAME);

    @ObjectClassDefinition(name = "Groovy Script Configuration Service")
    @interface Configuration {

        @AttributeDefinition(name = "Email Enabled?",
            description = "Check to enable email notification on completion of script execution.")
        boolean emailEnabled() default false;

        @AttributeDefinition(name = "Email Recipients",
            description = "Email addresses to receive notification.", cardinality = 20)
        String[] emailRecipients() default {};

        @AttributeDefinition(name = "Script Execution Allowed Groups",
            description = "List of group names that are authorized to use the console.  By default, only the 'admin' " +
                "user has permission to execute scripts.",
            cardinality = 20)
        String[] allowedGroups() default {};

        @AttributeDefinition(name = "Scheduled Jobs Allowed Groups",
            description = "List of group names that are authorized to schedule jobs.  By default, only the 'admin' " +
                "user has permission to schedule jobs.",
            cardinality = 20)
        String[] allowedScheduledJobsGroups() default {};

        @AttributeDefinition(name = "Vanity Path Enabled?",
            description = "Enables /groovyconsole vanity path.")
        boolean vanityPathEnabled() default false;

        @AttributeDefinition(name = "Audit Disabled?", description = "Disables auditing of script execution history.")
        boolean auditDisabled() default false;

        @AttributeDefinition(name = "Display All Audit Records?",
            description = "If enabled, all audit records (including records for other users) will be displayed in the" +
                " console history.")
        boolean auditDisplayAll() default false;

        @AttributeDefinition(name = "Thread Timeout",
            description = "Time in seconds that scripts are allowed to execute before being interrupted.  If 0, no " +
                "timeout is enforced.")
        long threadTimeout() default 0;
    }

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    private boolean emailEnabled;

    private Set<String> emailRecipients;

    private Set<String> allowedGroups;

    private Set<String> allowedServices;

    private boolean auditDisabled;

    private boolean displayAllAuditRecords;

    private long threadTimeout;

    @Override
    public boolean hasPermission(final SlingHttpServletRequest request) {
        return isAdminOrAllowedGroupMember(request, allowedGroups);
    }

    @Override
    public boolean isEmailEnabled() {
        return emailEnabled;
    }

    @Override
    public Set<String> getEmailRecipients() {
        return emailRecipients;
    }

    @Override
    public boolean isAuditDisabled() {
        return auditDisabled;
    }

    @Override
    public boolean isDisplayAllAuditRecords() {
        return displayAllAuditRecords;
    }

    @Override
    public long getThreadTimeout() {
        return threadTimeout;
    }

    @Override
    public Set<String> getAllowedServices() {
        return ImmutableSet.of("com.adobe.acs.commons.groovy.+");
    }

    @Activate
    @Modified
    void activate(final Configuration configuration) {
        emailEnabled = configuration.emailEnabled();
        emailRecipients = ImmutableSet.copyOf(configuration.emailRecipients());
        allowedGroups = ImmutableSet.copyOf(configuration.allowedGroups());
        auditDisabled = configuration.auditDisabled();
        displayAllAuditRecords = configuration.auditDisplayAll();
        threadTimeout = configuration.threadTimeout();
    }

    private boolean isAdminOrAllowedGroupMember(final SlingHttpServletRequest request, final Set<String> groupIds) {
        boolean allowed = false;

        try (final ResourceResolver resourceResolver = resourceResolverFactory.getServiceResourceResolver(AUTH_INFO)) {
            final User user = (User) resourceResolver.adaptTo(UserManager.class)
                .getAuthorizable(request.getUserPrincipal());

            if (user != null) {
                if (user.isAdmin()) {
                    allowed = true;
                } else {
                    final Iterator<Group> groups = user.memberOf();

                    while (groups.hasNext()) {
                        final Group group = groups.next();

                        if (groupIds.contains(group.getID())) {
                            allowed = true;
                            break;
                        }
                    }
                }
            }
        } catch (LoginException e) {
            LOG.error("error authenticating service resource resolver", e);
        } catch (RepositoryException e) {
            LOG.error("error checking group membership for user principal: {}", request.getUserPrincipal(), e);
        }

        return allowed;
    }
}
